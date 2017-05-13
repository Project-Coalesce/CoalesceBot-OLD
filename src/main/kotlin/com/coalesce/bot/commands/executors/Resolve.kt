package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.Embeddables
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import com.google.inject.Inject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class Resolve @Inject constructor(private val executor: ExecutorService) : Embeddables {
    @RootCommand(
            name = "Resolve",
            aliases = arrayOf("resolver", "url"),
            description = "Resolves URL shortened links.",
            permission = "commands.resolve",
            userCooldown = 15.0,
            type = CommandType.INFORMATION
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, "You need to specify a URL to resolve.")
            return
        }
        val url = context.args.joinToString(separator = "%20")
        executor.submit {
            try {
                context.channel.sendTyping()
                val resolved = getFinalUrl(url)
                context(embed().field("Receiver", context.author, true).field("Resolved", resolved, true)) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(35, TimeUnit.SECONDS) } }
            } catch (ex: Exception) {
                context(embed().field("Receiver", context.author, true).field("Error", "Couldn't resolve the URL.", true)) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(35, TimeUnit.SECONDS) } }
            }
        }
    }

    @Throws(Exception::class)
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