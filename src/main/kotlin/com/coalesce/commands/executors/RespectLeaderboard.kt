package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

@Command(name = "RespectsLeaderboard", aliases = arrayOf("fboard", "leaderboard", "lboard"), description = "Shows the respect command (!f) leaderboard", permission = "commands.respectLeaderboard")
class RespectLeaderboard : CommandExecutor() {
    val timeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS)
    var lastUsed: Long = -1

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (lastUsed == -1.toLong() || (System.currentTimeMillis() + timeout) <= lastUsed) {
            lastUsed = System.currentTimeMillis()

            // Reading
            val leaderboard = File(Constants.DATA_DIRECTORY, "leaderboard.json")
            if (!leaderboard.exists()) throw CommandError("Sadly nobody paid respects yet.")

            val respectLeaderboardJSON = JSONObject(leaderboard.readText(Charset.forName("UTF-8")))
            var respectLeaderboard: MutableList<User> = ArrayList()

            respectLeaderboardJSON.toMap().forEach({ k, _ -> respectLeaderboard.add(Bot.instance.jda.getUserById(k)) })
            Collections.sort(respectLeaderboard, { o1, o2 -> respectLeaderboardJSON.getInt(o1.id) - respectLeaderboardJSON.getInt(o2.id) })
            if (respectLeaderboard.size > 10) respectLeaderboard = respectLeaderboard.subList(0, 10)

            val builder = EmbedBuilder()
            val positionStr = StringBuilder()
            val nameStr = StringBuilder()
            val respectsPaidStr = StringBuilder()
            var pos = 1

            respectLeaderboard.forEach {
                positionStr.append("#$pos\n")
                nameStr.append("${it.name}\n")
                respectsPaidStr.append("${respectLeaderboardJSON.getInt(it.id)}\n")
                pos++
            }

            builder.addField("Position", positionStr.toString(), true).addField("Name", nameStr.toString(), true).addField("Respects", respectsPaidStr.toString(), true)

            channel.sendMessage(builder.build()).queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
        } else throw CommandError("This command is in cooldown for ${BigDecimal(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastUsed)).setScale(2, RoundingMode.HALF_EVEN)} seconds.")
    }
}