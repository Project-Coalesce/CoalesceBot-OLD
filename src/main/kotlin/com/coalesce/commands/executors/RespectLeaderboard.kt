package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.google.gson.Gson
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.concurrent.TimeUnit

@Command(name = "Respects Leaderboard", aliases = arrayOf("fboard", "leaderboard", "lboard"), description = "Press l to view leaderboard", permission = "commands.respectLeaderboard")
class RespectLeaderboard : CommandExecutor() {
    val timeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS)
    var lastUsed : Long = 0

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if(System.currentTimeMillis() - lastUsed > timeout){
            lastUsed = System.currentTimeMillis()

            // Reading
            val leaderboard = File(Constants.DATA_DIRECTORY, "leaderboard.json")
            if (!leaderboard.exists()) throw CommandError("Sadly nobody paid respects yet.")

            val respectLeaderboardJSON = Constants.GSON.fromJson(FileReader(leaderboard), mutableMapOf<String, Any?>()::class.java)
            var respectLeaderboard : MutableList<User> = ArrayList()

            respectLeaderboardJSON.forEach({ k, v -> respectLeaderboard.add(Bot.instance.jda.getUserById(k)) })
            Collections.sort(respectLeaderboard, { o1, o2 -> respectLeaderboardJSON[o1.id] as Int - respectLeaderboardJSON[o2.id] as Int })
            if(respectLeaderboard.size > 10) respectLeaderboard = respectLeaderboard.subList(0, 10)

            val builder = EmbedBuilder()
            val positionStr = StringBuilder()
            val nameStr = StringBuilder()
            val respectsPaidStr = StringBuilder()
            var pos = 1

            respectLeaderboard.forEach {
                positionStr.append("#$pos\n")
                nameStr.append("${it.name}\n")
                respectsPaidStr.append("${respectLeaderboardJSON[it.id] as Int}\n")
                pos ++
            }

            builder.addField("Position", positionStr.toString(), true)
            builder.addField("Name", nameStr.toString(), true)
            builder.addField("Respects", respectsPaidStr.toString(), true)

            channel.sendMessage(builder.build()).queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
        }else throw CommandError("This command is in cooldown for " + TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastUsed, TimeUnit.MILLISECONDS) + " seconds.")
    }
}