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
        description = "Defines a word or phrase with Urban Dictionary.",
        permission = "command.definition")
class Definition : CommandExecutor() {

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (args.isEmpty()) {
            throw CommandError("Please follow the correct syntax: %s", annotation.usage)
        }
        val phrase = args.joinToString(separator = "+")

        val url = URL("http://api.urbandictionary.com/v0/define?term=" + phrase)
        val scanner = Scanner(url.openStream())

        val jsonString = StringBuilder()
        while (scanner.hasNextLine()) {
            jsonString.append(scanner.nextLine())
        }
        scanner.close()

        val gson = GsonBuilder().create()
        val json = gson.fromJson(jsonString.toString(), JsonElement::class.java).asJsonObject

        if (json.get("result_type").asString == "no_results") {
            throw CommandError("No results found for %s", phrase)
        }

        val result = json.get("list").asJsonArray.get(0).asJsonObject
        val builder = EmbedBuilder().setColor(Color.BLUE).setAuthor(message.author.name, null, message.author.avatarUrl)
                .setTitle("Urban Dictionary Definition", result.get("permalink").asString)
                .addField("Word", result.get("word").asString, true)
                .addField("Definition", result.get("definition").asString, true)
        channel.sendMessage(builder.build()).queue { it.delete().queueAfter(1, TimeUnit.MINUTES) }
    }
}
