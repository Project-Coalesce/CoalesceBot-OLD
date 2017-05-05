package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.ifwithDo
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class UrbanDefinition(@Inject val executorService: ExecutorService) {
    @RootCommand(
            name = "Definition",
            aliases = arrayOf("define", "dictionary", "urban"), description = "Defines a word or phrase with Urban Dictionary.",
            permission = "command.definition",
            globalCooldown = 5.0,
            type = CommandType.INFORMATION
    )
    fun execute(context: RootCommandContext) {
        fun mention(text: String) {
            context.send(context.author, text)
        }
        if (context.args.isEmpty()) {
            mention("Please specify a word to chec the definition of.")
            return
        }
        val phrase = context.args.joinToString(separator = "+")

        executorService.submit {
            try {
                val url = URL("http://api.urbandictionary.com/v0/define?term=$phrase")

                val map = mutableMapOf<String, Any?>()
                url.openStream().use {
                    it.reader().use {
                        map.putAll(gson.fromJson(it, map::class.java))
                    }
                }
                if (map["result_type"] as? String ?: "no_results" == "no_results") {
                    mention("No results were found for $phrase.")
                    return@submit
                }
                val obj = map["list"] as? MutableMap<*, *> ?: run { mention("No definitions were found in list."); return@submit }
                val permalink = obj["permalink"] as? String ?: run { mention("The definition didn't have a permalink."); return@submit }
                val word = obj["word"] as? String ?: run { mention("The definition didn't have an owning word."); return@submit }
                val definition = obj["definition"] as? String ?: run { mention("The definition didn't have a definition."); return@submit }
                val builder = EmbedBuilder().apply {
                    setColor(Color.BLUE)
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setTitle("Urban Dictionary Definition", permalink)
                    addField("Word", word, true)
                    addField("Definition", definition, true)
                }
                context.send(builder.build()) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(35, TimeUnit.SECONDS) } }
            } catch (ex: Exception) {
                val embedBuilder = EmbedBuilder()

                embedBuilder.setColor(Color(232, 46, 0))
                embedBuilder.setTitle("Error", null)
                embedBuilder.setDescription("An error occured while trying to handle that command:\n${ex.javaClass.name}: ${ex.message}")

                context.send(embedBuilder.build())
            }
        }
    }
}