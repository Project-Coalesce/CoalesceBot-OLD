package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

@Command(name = "RespectsLeaderboard", aliases = arrayOf("fboard", "leaderboard", "lboard"), description = "Shows the respect command (!f) leaderboard", permission = "commands.respectLeaderboard",
        globalCooldown = 5, type = CommandType.FUN)
class RespectLeaderboard : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        val leaderboard = File(Constants.DATA_DIRECTORY, "leaderboard.json")
        if (!leaderboard.exists()) {
            throw CommandError("Sadly nobody has paid respects yet.")
        }

        val leaderboardJson = mutableMapOf<String, Any?>()
        leaderboard.inputStream().use {
            it.reader().use {
                leaderboardJson.putAll(Constants.GSON.fromJson(it, leaderboardJson::class.java))
            }
        }
        val respects = mutableListOf<User>()
        leaderboardJson.forEach { key, _ -> respects.add(Bot.instance.jda.getUserById(key)) }
        Collections.sort(respects, { first, second -> (leaderboardJson[second.id] as Double).toInt() - (leaderboardJson[first.id] as Double).toInt() })
        if (respects.size > 10) {
            val back = mutableListOf<User>()
            back.addAll(respects)
            respects.clear()
            respects.addAll(back.subList(0, 10))
        }

        val builder = EmbedBuilder()
        val positionStr = StringBuilder()
        val nameStr = StringBuilder()
        val respectsPaidStr = StringBuilder()

        respects.forEachIndexed { index, it ->
            positionStr.append("#${index + 1}\n")
            nameStr.append("${it.name}\n")
            respectsPaidStr.append("${(leaderboardJson[it.id] as Double).toInt()}\n")
        }

        builder.addField("Position", positionStr.toString(), true).addField("Name", nameStr.toString(), true).addField("Respects", respectsPaidStr.toString(), true)

        channel.sendMessage(builder.build()).queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
    }
}