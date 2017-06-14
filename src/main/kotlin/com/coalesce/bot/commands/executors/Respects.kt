package com.coalesce.bot.commands.executors

import com.coalesce.bot.*
import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.ifwithDo
import com.coalesce.bot.utilities.limit
import com.coalesce.bot.utilities.parseDouble
import com.coalesce.bot.utilities.quietly
import com.google.gson.reflect.TypeToken
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.awt.Color
import java.io.DataOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

enum class RespectReactions(val message: String,
                            val amount: Double,
                            val delay: Double,
                            val rating: String,
                            val emoteName: Optional<String> = Optional.empty(),
                            val emoteId: Optional<Long> = Optional.empty()) {
    NOT_DANK_ENOUGH("Not Dank Enough", -1.0, 1260.0, "0/10", emoteId = Optional.of(304043388523511808L)),
    FUNNY("Funny 🥚🥚🇩 🇪", 1.0, 860.0, "6.9/10", emoteName = Optional.of("😂")),
    LIT("Lit Fam", 2.0, 720.0, "8.5/10", emoteName = Optional.of("🔥")),
    DANK("Dank", 3.0, 1260.0, "10/10", emoteId = Optional.of(318557118791680000L))
}

class Respects @Inject constructor(val bot: Main): Embeddables {
    @RootCommand(
            name = "Respects",
            aliases = arrayOf("f", "nahusdream"),
            description = "Over-engineered meme command (Press F to pay respects)",
            permission = "commands.respects",
            type = CommandType.FUN,
            globalCooldown = 6.0 * 3600.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "Respects have been paid! **+8 respect**") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(60, TimeUnit.SECONDS) } }

        transaction(context.author, 8.0)
    }

    @JDAListener
    fun message(event: MessageReceivedEvent, context: EventContext) {
        //308791021343473675L
        if (!event.message.attachments.isEmpty() && event.channel.idLong == 315565346973024256L) {
            RespectReactions.values().forEach {
                if (it.emoteName.isPresent) {
                    event.message.addReaction(it.emoteName.get()).queue()
                } else quietly {
                    event.message.addReaction(event.guild.getEmoteById(it.emoteId.get())).queue()
                }
            }
        }
    }

    @JDAListener
    fun react(event: MessageReactionAddEvent, context: EventContext) {
        if (event.user.isBot || bot.listener.isBlacklisted(event.user)) return //Bad boys can't do this

        if (event.channel.idLong == 308791021343473675L/* #memes */) {
            RespectReactions.values().forEach {
                if (it.emoteName.isPresent && event.reaction.emote.name == it.emoteName.get() && context.runChecks(event.user, event.channel!!, it.delay, it.name)) {
                    event.channel.getMessageById(event.messageId).queue { message ->
                        dank(event.channel!!, event.user, message.author, event.jda, it)
                    }
                    return
                }
            }

            if (event.reaction.guild != null && !event.reaction.emote.emote.isManaged) {
                RespectReactions.values().forEach {
                    if (it.emoteId.isPresent && it.emoteId.get() == event.reaction.emote.idLong && context.runChecks(event.user, event.channel!!, it.delay, it.name)) {
                        event.channel.getMessageById(event.messageId).queue { message ->
                            dank(event.channel!!, event.user, message.author, event.jda, it)
                        }
                        return
                    }
                }
            }
        }
    }

    private fun dank(channel: MessageChannel, from: User, to: User, jda: JDA, reaction: RespectReactions) {
        if (to == from || to == jda.selfUser) {
            channel.sendMessage("* Invalid user").queue()
            return
        }
        transaction(to, reaction.amount)
        channel.sendMessage("${to.asMention}: Meme rating from ${from.name}: \"${reaction.message}\" - ${reaction.rating}" +
                "**${if (reaction.amount > 0) "+" else ""}${reaction.amount.toInt()} respect**").queue()
    }

    @SubCommand(
            name = "reset",
            permission = "commands.respects.reset",
            globalCooldown = 0.0
    )
    fun resetScore(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            context("* You need to mention someone to reset scores of.")
            return
        }
        val user = context.message.mentionedUsers.first()
        val file = respectsLeaderboardsFile
        if (!file.exists()) generateFile(file)

        val serializer = RespectsLeaderboardSerializer(file)
        val map = serializer.read()

        if (!map.containsKey(user.id)) {
            context("* This user already is empty.")
            return
        }

        map.remove(user.id)
        serializer.write(map)
        context(context.author, "Reset scores of ${user.asMention}.")
    }

    @SubCommand(
            name = "edit",
            permission = "commands.respects.edit",
            globalCooldown = 0.0
    )
    fun scoreEdit(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty() || context.args.size < 2) {
            context("* Usage: !f edit <mention> <amount>")
            return
        }
        val user = context.message.mentionedUsers.first()

        val file = respectsLeaderboardsFile
        if (!file.exists()) generateFile(file)

        val serializer = RespectsLeaderboardSerializer(file)
        val map = serializer.read()
        map[user.id] = (map[user.id] ?: 0.0) + (context.args[1].parseDouble() ?: run {
            context("* Amount specified '${context.args[1]}' is not a valid value.")
            return
        })

        serializer.write(map)
        context(context.author, "Set scores of ${user.asMention} to ${map[user.id]}.")
    }

    @SubCommand(
            name = "set",
            permission = "commands.respects.set",
            globalCooldown = 0.0
    )
    fun scoreSet(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty() || context.args.size < 2) {
            context("* Usage: !f set <mention> <amount>")
            return
        }
        val user = context.message.mentionedUsers.first()

        val file = respectsLeaderboardsFile
        if (!file.exists()) generateFile(file)

        val serializer = RespectsLeaderboardSerializer(file)
        val map = serializer.read()
        map[user.id] = context.args[1].parseDouble() ?: run {
            context("* Amount specified '${context.args[1]}' is not a valid value.")
            return
        }

        serializer.write(map)
        context(context.author, "Set scores of ${user.asMention} to ${map[user.id]}.")
    }

    @SubCommand(
            name = "leaderboard",
            aliases = arrayOf("fboard", "lboard", "board", "respectsboard", "rboard", "ftop", "top"),
            permission = "commands.respects.leaderboard",
            globalCooldown = 30.0
    )
    fun fboard(context: SubCommandContext) {
        val file = respectsLeaderboardsFile
        synchronized(file) {
            if (!file.exists()) {
                context("* Respects leaderboard is empty.")
                return
            }

            val serializer = RespectsLeaderboardSerializer(file)
            val map = serializer.read()

            var top10 = mutableListOf<Member>()
            val amountPositions = mutableListOf<Double>()

            map.forEach { key, value ->
                quietly {
                    val member = context.message.guild.getMember(bot.jda.getUserById(key)) ?: return@quietly
                    if (value is Double) {
                        top10.add(member)
                        amountPositions.add(value)
                    }
                }
            }

            Collections.sort(amountPositions)
            Collections.reverse(amountPositions)
            top10 = top10.subList(0, Math.min(top10.size, 10))
            Collections.sort(top10, { second, first -> (map[first.user.id] as Double).toInt() - (map[second.user.id] as Double).toInt() })
            if (top10.size > 10) {
                val back = mutableListOf<Member>()
                back.addAll(top10.subList(0, 10))
                top10.clear()
                top10.addAll(back)
            }

            val positionStr = StringBuilder()
            val nameStr = StringBuilder()
            val respectsPaidStr = StringBuilder()

            top10.forEach {
                val value = map[it.user.id] as Double

                positionStr.append("#${amountPositions.indexOf(value) + 1}\n")
                nameStr.append("${(it.effectiveName).limit(16)}\n")
                respectsPaidStr.append("${value.toInt()}\n")
            }

            val member = context.message.member
            if(map.containsKey(member.user.id) && !top10.contains(member)) {
                val value = map[member.user.id] as Double

                positionStr.append("...\n#${amountPositions.indexOf(value) + 1}")
                nameStr.append("...\n${(member.effectiveName).limit(16)}")
                respectsPaidStr.append("...\n${value.toInt()}")
            }

            context(embed().apply {
                setColor(Color(0x5ea81e))

                addField("Position", positionStr.toString(), true)
                addField("Name", nameStr.toString(), true)
                addField("Respects", respectsPaidStr.toString(), true)
            })
        }
    }

    private fun transaction(user: User, amount: Double) {
        val file = respectsLeaderboardsFile
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

    private fun generateFile(file: File) {
        file.createNewFile()
        if (respectsLeaderboardsFileOld.exists()) {
            val oldMap = gson.fromJson<MutableMap<String, Double>>(respectsLeaderboardsFileOld.readText(), object: TypeToken<HashMap<String, Double>>() {}.type)

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