package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Command(name = "Resolve", aliases = arrayOf("resolver", "url"), usage = "<url>", description = "Resolves URL shortened links.", permission = "commands.resolve")
class Resolve : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (args.isEmpty()) {
            throw CommandError("Please use the correct syntax: %s", annotation.usage)
        }
        val url = args.joinToString("%20")
        Bot.instance.executor.execute({
            try {
                val resolved = getFinalUrl(url)
                message.channel.sendMessage(MessageBuilder().append(message.author).append(": The URL was resolved to ").append(resolved).build()).complete()
            } catch (ex: IOException) {
                message.channel.sendMessage(MessageBuilder().append(message.author).appendFormat(": Couldn't resolve the URL.").build()).complete()
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