package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.gson
import com.coalesce.bot.respectsLeaderboardsFile
import com.coalesce.bot.utilities.ifwithDo
import com.coalesce.bot.utilities.limit
import com.google.gson.internal.LinkedTreeMap
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.TimeUnit

class Respects {
    @RootCommand(
            name = "Respects",
            aliases = arrayOf("f", "nahusdream"),
            description = "Over-engineered meme command (Press F to pay respects)",
            permission = "commands.respects",
            type = CommandType.FUN,
            globalCooldown = 6.0 * 3600.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "Respects have been paid!") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(60, TimeUnit.SECONDS) } }

        val file = respectsLeaderboardsFile
        synchronized(file) {
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            val json = LinkedTreeMap<String, Any?>()
            if (file.exists()) {
                file.inputStream().use {
                    it.reader().use {
                        json.putAll(gson.fromJson(it, json::class.java))
                    }
                }
            }

            val id = context.author.id
            json[id] = json[id] as? Double ?: 0.0 + 1.0
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            Files.write(file.toPath(), gson.toJson(json).toByteArray(), StandardOpenOption.WRITE)
        }
    }
}

class RespectsLeaderboard @Inject constructor(val jda: JDA) {
    @RootCommand(
            name = "leaderboard",
            aliases = arrayOf("fboard", "lboard", "board", "respectsboard", "rboard", "ftop"),
            description = "Displays the leaders of Respects.",
            permission = "commands.leaderboard",
            type = CommandType.FUN,
            globalCooldown = 30.0
    )
    fun execute(context: RootCommandContext) {
        val file = respectsLeaderboardsFile
        synchronized(file) {
            if (!file.exists()) {
                context("Sadly nobody has paid respects yet.")
                return
            }
            val json = mutableMapOf<String, Any?>()
            file.inputStream().use {
                it.reader().use {
                    json.putAll(gson.fromJson(it, json::class.java))
                }
            }
            val respects = mutableListOf<Member>()
            json.forEach { key, _ ->
                val member = context.message.guild.getMember(jda.getUserById(key))
                if (member != null) {
                    respects.add(member)
                }
            }
            Collections.sort(respects, { second, first -> (json[first.user.id] as Double).toInt() - (json[second.user.id] as Double).toInt() })
            if (respects.size > 10) {
                val back = mutableListOf<Member>()
                back.addAll(respects.subList(0, 10))
                respects.clear()
                respects.addAll(back)
            }

            val builder = EmbedBuilder()
            val positionStr = StringBuilder()
            val nameStr = StringBuilder()
            val respectsPaidStr = StringBuilder()

            respects.forEachIndexed { index, it ->
                positionStr.append("#${index + 1}\n")
                nameStr.append("${(it.nickname ?: it.effectiveName ?: it.user.name).limit(16)}\n")
                respectsPaidStr.append("${(json[it.user.id] as Double).toInt()}\n")
            }
            builder.addField("Position", positionStr.toString(), true)
                    .addField("Name", nameStr.toString(), true)
                    .addField("Respects", respectsPaidStr.toString(), true)
            context(builder)
        }
    }
}