package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.net.URL
import java.util.Scanner
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.concurrent.TimeUnit

@Command(name = "Definition",
        aliases = arrayOf("define", "dictionary"),
        usage = "<phrase>",
        description = "Need a definition? Ask me!",
        permission = "command.definition")
class Definition : CommandExecutor() {

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (args.size < 1) {
            throw CommandError("Please follow the correct syntax: %s", annotation.usage)
        }
        val phrase = args.copyOfRange(1, args.size).joinToString(separator = "+")

        val url = URL("http://api.urbandictionary.com/v0/define?term=" + phrase)
        val scanner = Scanner(url.openStream())

        val jsonString = ""
        while (scanner.hasNext()) {
            jsonString.plus(scanner.next() + " ")
        }
        scanner.close()

        val gson = GsonBuilder().create()
        val json = gson.fromJson(jsonString, JsonElement::class.java).asJsonObject

        if (json.get("result_type").asString == "no_results") {
            throw CommandError("No results found for %s", phrase)
        }

        val result = json.get("list").asJsonArray.get(0).asJsonObject
        val builder = EmbedBuilder().setColor(Color.BLUE).setAuthor(message.author.name, null, message.author.avatarUrl)
                .addField("Word", result.get("word").asString, true)
                .addField("Definition", result.get("definition").asString, true)
                .addField("Permalink", result.get("permalink").asString, true)
        channel.sendMessage(builder.build()).queue { it.delete().queueAfter(1, TimeUnit.MINUTES) }
    }
}
