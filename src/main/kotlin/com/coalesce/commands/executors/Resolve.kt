package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

@Command(name = "Resolve", aliases = arrayOf("resolver", "url"), usage = "<url>", description = "Resolves URL shortened links.", permission = "commands.resolve",
        cooldown = 5,type = CommandType.INFORMATION)
class Resolve : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (args.isEmpty()) {
            throw CommandError("Please use the correct syntax: %s", annotation.usage)
        }
        val url = args.joinToString("%20")
        Bot.instance.executor.execute({
            try {
                message.channel.sendTyping()
                val resolved = getFinalUrl(url)
                message.channel.sendMessage(EmbedBuilder().setColor(Color(0.0f, 0.5f, 0.0f)).addField("Receiver", message.author.asMention, true).addField("Resolved", resolved, true).build()).complete().delete().queueAfter(15, TimeUnit.SECONDS)
            } catch (ex: Exception) { // stop exceptions in console cause of their cancer shit
                message.channel.sendMessage(EmbedBuilder().setColor(Color(0.5f, 0.0f, 0.0f)).addField("Receiver", message.author.asMention, true).addField("Error", "Couldn't resolve the URL", true).build()).complete().delete().queueAfter(10, TimeUnit.SECONDS)
            }
        })
    }

    @Throws(IOException::class)
    private fun getFinalUrl(url: String): String {
        @Suppress("NAME_SHADOWING")
        var url = url
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            url = "http://" + url
        }

        val conn = URL(url).openConnection() as HttpURLConnection
        conn.addRequestProperty("User-Agent", "Mozilla/4.76")
        conn.instanceFollowRedirects = false
        conn.connect()
        conn.inputStream

        if (conn.responseCode == HttpURLConnection.HTTP_MOVED_TEMP || conn.responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
            return getFinalUrl(conn.getHeaderField("Location"))
        }

        return url
    }
}