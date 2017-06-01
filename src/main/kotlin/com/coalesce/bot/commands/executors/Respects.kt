package com.coalesce.bot.commands.executors

import com.coalesce.bot.*
import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.ifwithDo
import com.coalesce.bot.utilities.limit
import com.google.gson.reflect.TypeToken
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.io.DataOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class Respects @Inject constructor(val bot: Main) {
    @RootCommand(
            name = "Respects",
            aliases = arrayOf("f", "nahusdream"),
            description = "Over-engineered meme command (Press F to pay respects)",
            permission = "commands.respects",
            type = CommandType.FUN,
            globalCooldown = 6.0 * 3600.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "Respects have been paid! You got +5 respects.") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(60, TimeUnit.SECONDS) } }

        transaction(context.author, 5.0)
    }

    @JDAListener
    fun react(event: MessageReactionAddEvent) {
        if (event.channel.idLong == 308791021343473675L) {
            if (event.reaction.emote.name == "<:dank:318557118791680000>") {
                reactionCheck(event, Consumer {
                    dank(event.guild, event.channel!!, event.user, it.author, event.jda)
                })
            } else if (event.reaction.emote.name == "<:lifehack:304043388523511808>") {
                reactionCheck(event, Consumer {
                    notDankEnough(event.guild, event.channel!!, event.user, it.author, event.jda)
                })
            }
        }
    }

    private fun dank(guild: Guild, channel: net.dv8tion.jda.core.entities.MessageChannel, from: User, to: User, jda: JDA) {
        if (from == to) {
            channel.sendMessage("* You see, doing that is what makes you not dank.").queue()
            return
        }
        if (to == jda.selfUser) {
            channel.sendMessage("* My shit's fucking lit ain't it").queue()
            return
        }
        transaction(to, 1.0)
        channel.sendMessage("${to.asMention}: Dank - 10/10 ${from.asMention} **+1 respect**").queue()
    }

    private fun notDankEnough(guild: Guild, channel: net.dv8tion.jda.core.entities.MessageChannel, from: User, to: User, jda: JDA) {
        if (from == to) {
            channel.sendMessage("* You such an emo kid.").queue()
            return
        }
        if (to == jda.selfUser) {
            channel.sendMessage("* Don't you dare say that about my messages.").queue()
            return
        }
        transaction(to, -1.0)
        channel.sendMessage("${to.asMention}: Not Dank Enough - 0/10 ${from.asMention} **-1 respect**").queue()
    }

    private fun transaction(user: User, amount: Double) {
        val file = respectsLeaderboardsFile
        synchronized(file) {
            if (!file.exists()) generateFile(file)

            val serializer = RespectsLeaderboardSerializer(file)
            val map = serializer.read()

            val id = user.id
            map[id] = (map[id] as? Double ?: 0.0) + amount
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            serializer.write(map)
        }
    }

    fun reactionCheck(event: MessageReactionAddEvent, consumer: Consumer<Message>) {
        event.channel.getMessageById(event.messageId).queue(consumer::accept)
    }

    fun generateFile(file: File) {
        file.createNewFile()
        if (respectsLeaderboardsFileOld.exists()) {
            val type = object: TypeToken<HashMap<String, Any?>>() {}
            val oldMap = gson.fromJson<MutableMap<String, Any?>>(respectsLeaderboardsFileOld.readText(), type.type)

            val repSerializer = RespectsLeaderboardSerializer(file)
            repSerializer.write(oldMap)

            val oldSize = respectsLeaderboardsFileOld.length()
            respectsLeaderboardsFileOld.delete()
            println("Updated reputation file to binary, removing ${oldSize - file.length()} bytes.")
        } else {
            file.outputStream().use {
                DataOutputStream(it).writeLong(-1L)
            }
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

            val serializer = RespectsLeaderboardSerializer(file)
            val map = serializer.read()

            var respects = mutableListOf<Member>()
            val amountPositions = mutableListOf<Double>()

            map.forEach { key, value ->
                val member = context.message.guild.getMember(jda.getUserById(key))
                if (member != null &&
                        value is Double && // For safety with json, in case the host manages to edit it into something else
                        value > 0) { // invalid/punished values shouldnt be accepted.
                    respects.add(member)
                    amountPositions.add(value)
                }
            }

            Collections.sort(amountPositions)
            respects = respects.subList(0, Math.min(respects.size, 10))
            Collections.sort(respects, { second, first -> (map[first.user.id] as Double).toInt() - (map[second.user.id] as Double).toInt() })
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

            respects.forEach {
                val value = map[it.user.id] as Double

                positionStr.append("#${amountPositions.indexOf(value) + 1}\n")
                nameStr.append("${(it.effectiveName).limit(16)}\n")
                respectsPaidStr.append("${value.toInt()}\n")
            }

            val member = context.message.member
            if(respects.contains(member) && respects.indexOf(member) > 10) {
                val value = map[member.user.id] as Double

                positionStr.append("...\n#${amountPositions.indexOf(value) + 1}")
                nameStr.append("...\n${(member.effectiveName).limit(16)}")
                respectsPaidStr.append("...\n${value.toInt()}")
            }
            builder.addField("Position", positionStr.toString(), true)
                    .addField("Name", nameStr.toString(), true)
                    .addField("Respects", respectsPaidStr.toString(), true)
            context(builder)
        }
    }
}