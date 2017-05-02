package com.coalesce.commands.executors

import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit

@Command(name = "Respects", aliases = arrayOf("f", "nahusdream"), description = "Over-engineered meme command (Press F to pay respects)", permission = "commands.respects",
        type = CommandType.FUN, globalCooldown = 6 * 3600)
class Respects : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        channel.sendMessage("Respects have been paid!").queue { it.delete().queueAfter(1, TimeUnit.MINUTES) }

        val leaderboard = File(Constants.DATA_DIRECTORY, "leaderboard.json")
        if (!leaderboard.parentFile.exists()) leaderboard.parentFile.mkdirs()
        val respectLeaderboardJSON = mutableMapOf<String, Any?>()
        if (leaderboard.exists()) {
            respectLeaderboardJSON.putAll(Constants.GSON.fromJson(FileReader(leaderboard), respectLeaderboardJSON::class.java))
        }

        val id = message.author.id
        if (respectLeaderboardJSON.containsKey(id)) {
            respectLeaderboardJSON[id] = respectLeaderboardJSON[id] as Double + 1.0
        } else {
            respectLeaderboardJSON[id] = 1.0
        }

        if (leaderboard.exists()) {
            leaderboard.delete()
        }
        leaderboard.createNewFile()
        Files.write(leaderboard.toPath(), Constants.GSON.toJson(respectLeaderboardJSON).toByteArray(), StandardOpenOption.WRITE)
    }
}
