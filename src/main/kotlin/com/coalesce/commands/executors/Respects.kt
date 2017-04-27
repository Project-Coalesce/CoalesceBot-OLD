package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.TimeUnit

@Command(name = "Respects", aliases = arrayOf("f", "nahusdream"), description = "Press F to pay respects", permission = "commands.respects")
class Respects : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (Bot.instance.respectsLastUse != -1f) {
            val lastUse = Bot.instance.respectsLastUse
            if ((lastUse + TimeUnit.HOURS.toMillis(6.toLong())) > System.currentTimeMillis()) {
                channel.sendMessage("You can pay respects again in ${Constants.DECIMAL_FORMAT.format(TimeUnit.MILLISECONDS.toHours(((lastUse + TimeUnit.HOURS.toMillis(6)-System.currentTimeMillis()).toLong())))} hours.")
                        .queue { it.delete().queueAfter(5, TimeUnit.SECONDS) }
                return
            }
        }
        Bot.instance.respectsLastUse = System.currentTimeMillis().toFloat()
        channel.sendMessage("Respects have been paid!").queue { it.delete().queueAfter(1, TimeUnit.MINUTES) }

        val leaderboard = File(Constants.DATA_DIRECTORY, "leaderboard.json")
        if (!leaderboard.parentFile.exists()) leaderboard.parentFile.mkdirs()
        var respectLeaderboardJSON : MutableMap<String, Any?> = HashMap()
        if (leaderboard.exists()) Constants.GSON.fromJson(FileReader(leaderboard), mutableMapOf<String, Any?>()::class.java)

        val id = message.author.id
        if (respectLeaderboardJSON.containsKey(id)) respectLeaderboardJSON[id] = respectLeaderboardJSON[id] as Int + 1
        else respectLeaderboardJSON[id] = 1

        if (leaderboard.exists()) leaderboard.delete()
        leaderboard.createNewFile()
        Constants.GSON.toJson(respectLeaderboardJSON, FileWriter(leaderboard))
    }
}
