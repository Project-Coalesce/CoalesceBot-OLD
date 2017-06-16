package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.ifwithDo
import com.google.gson.JsonElement
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.net.URL
import java.util.Scanner
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class UrbanDefinition @Inject constructor(val executorService: ExecutorService) : Embeddables {
    @RootCommand(
            name = "Definition",
            aliases = arrayOf("define", "dictionary", "urban"), description = "Defines a word or phrase with Urban Dictionary.",
            permission = "commands.definition",
            globalCooldown = 15.0,
            type = CommandType.INFORMATION
    )
    fun execute(context: RootCommandContext) {
        fun mention(text: String) {
            context(context.author, text)
        }

        if (context.args.isEmpty()) {
            throw ArgsException("Please specify a word to check the definition of.")
        }
        val phrase = context.args.joinToString(separator = "+")

        executorService.submit {
            try {
                val url = URL("http://api.urbandictionary.com/v0/define?term=$phrase")

                val scanner = Scanner(url.openStream())

                val stringBuilder = StringBuilder()
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine())
                }
                scanner.close()

                val json = gson.fromJson(stringBuilder.toString(), JsonElement::class.java).asJsonObject
                if (json.get("result_type").asString == "no_results") {
                    mention("No definitions found!")
                    return@submit
                }

                val firstResult = json.get("list").asJsonArray.get(0).asJsonObject
                val builder = EmbedBuilder().apply {
                    setColor(Color.BLUE)
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setTitle("Urban Dictionary Definition", firstResult.get("permalink").asString)
                    addField("Word", firstResult.get("word").asString, true)
                    addField("Definition", firstResult.get("definition").asString, true)
                }
                context(builder) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(35, TimeUnit.SECONDS) } }
            } catch (ex: Exception) {
                context(embed().setColor(Color(232, 46, 0)).setTitle("Error", null).setDescription("An error occured with that command:\n" +
                        "${ex.javaClass.name}: ${ex.message}\nPlease report this to project coalesce developers."))
                ex.printStackTrace()
            }
        }
    }
}
