package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.net.URL
import java.util.concurrent.TimeUnit

@Command(name = "Definition",
        aliases = arrayOf("define", "dictionary", "urban"),
        usage = "<phrase>",
        description = "Defines a word or phrase with Urban Dictionary.",
        permission = "command.definition",
        globalCooldown = 5,
        type = CommandType.INFORMATION)
class Definition : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (args.isEmpty()) {
            throw CommandError("Please follow the correct syntax: %s", annotation.usage)
        }
        val phrase = args.joinToString(separator = "+")

        Bot.instance.executor.execute {
            try {
                val url = URL("http://api.urbandictionary.com/v0/define?term=$phrase")

                val map = mutableMapOf<String, Any?>()
                url.openStream().use {
                    it.reader().use {
                        map.putAll(Constants.GSON.fromJson(it, map::class.java))
                    }
                }
                if (map["result_type"] as? String ?: "no_results" == "no_results") {
                    throw CommandError("No results were found for $phrase.")
                }
                val obj = (map["list"] as? MutableMap<*, *> ?: throw CommandError("No definitions were found in list."))
                val permalink = obj["permalink"] as? String ?: throw CommandError("The definition didn't have a permalink.")
                val word = obj["word"] as? String ?: throw CommandError("The definition didn't have an owning word.")
                val definition = obj["definition"] as? String ?: throw CommandError("The definition didn't have a definition.")
                val builder = EmbedBuilder().apply {
                    setColor(Color.BLUE)
                    setAuthor(message.author.name, null, message.author.avatarUrl)
                    setTitle("Urban Dictionary Definition", permalink)
                    addField("Word", word, true)
                    addField("Definition", definition, true)
                }
                channel.sendMessage(builder.build()).queue { it.delete().queueAfter(35, TimeUnit.SECONDS) }
            } catch (ex: Exception) {
                if (ex is CommandError) {
                    channel.sendMessage(MessageBuilder().append(message.author).appendFormat(": %s", ex.message).build()).queue()
                } else {
                    val embedBuilder = EmbedBuilder()

                    embedBuilder.setColor(Color(232, 46, 0))
                    embedBuilder.setTitle("Error", null)
                    embedBuilder.setDescription("An error occured while trying to handle that command:\n${ex.javaClass.name}: ${ex.message}")

                    channel.sendMessage(embedBuilder.build()).queue()
                }
            }
        }
    }
}
