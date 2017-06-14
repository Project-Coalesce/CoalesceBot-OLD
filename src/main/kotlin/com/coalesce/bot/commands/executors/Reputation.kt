package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.commands.*
import com.coalesce.bot.reputation.ReputationManager
import com.coalesce.bot.reputation.ReputationTransaction
import com.coalesce.bot.respectsLeaderboardsFile
import com.coalesce.bot.utilities.limit
import com.coalesce.bot.utilities.parseDouble
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.awt.Color
import java.util.*

class Reputation @Inject constructor(val bot: Main, val reputation: ReputationManager): Embeddables {
    private val messagesMap = mutableMapOf<User, Int>()

    @RootCommand(
            name = "reputation",
            type = CommandType.INFORMATION,
            permission = "commands.reputation",
            aliases = arrayOf("rep", "reput"),
            description = "View your reputation.",
            userCooldown = 10.0,
            globalCooldown = 0.0
    )
    fun execute(context: RootCommandContext) {
        val target = if (context.message.mentionedUsers.isEmpty()) context.author else context.message.mentionedUsers.first()
        val rep = reputation[target]

        val transactionsString =
            if (rep.transactions.isEmpty()) {
                "None."
            } else {
                rep.transactions.joinToString(separator = "\n") { "**${if (it.amount >= 0) "+" else ""}${it.amount.toInt()}**: ${it.message}" }
            }

        context.send(
            embed().apply {
                setColor(Color(0x5ea81e))

                setTitle("${if (target == context.author) "You have" else "${target.name} has"} ${rep.total.toInt()} reputation.", null)
                addField("Recent", transactionsString, false)
            }
        )
    }

    @JDAListener
    fun messageReceived(event: MessageReceivedEvent, context: EventContext) {
        messagesMap[event.author] = (messagesMap[event.author] ?: 0) + 1

        if (messagesMap[event.author]!! >= 25 + Math.max((reputation[event.author].total / 5.0).toInt(), 700)) {
            reputation[event.author].transaction(ReputationTransaction("Award for sending ${messagesMap[event.author]} messages", 10.0),
                    event.channel, event.guild.getMember(event.author))
            messagesMap[event.author] = 0
        }
    }

    @JDAListener
    fun react(event: MessageReactionAddEvent, context: EventContext) {
        if (event.reaction.emote.name.contains("✌")) {
            if (context.runChecks(event.user, event.channel!!, 360.0, "thank")) {
                event.channel.getMessageById(event.messageId).queue {
                    transaction(event.guild, event.channel!!, event.user, it.author, event.jda, "thanked you",
                            { origin, _ -> Math.min((origin / 80.0) + 20.0, 100.0) })
                }
            }
        } else if (event.reaction.emote.name.contains("👎")) {
            if (context.runChecks(event.user, event.channel!!, 720.0, "downrate")) {
                event.channel.getMessageById(event.messageId).queue {
                    transaction(event.guild, event.channel!!, event.user, it.author, event.jda, "down-rated you", { _, _ -> -10.0 })
                }
            }
        }
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
        val map = reputation.readRawData()
        if (map.containsKey(user.idLong)) {
            context("* This user already is empty.")
            return
        }
        map.remove(user.idLong)
        reputation.clearCache() // Prevent cached data to override saved data.
        reputation.writeRawData(map)

        context(context.author, "Reset scores of ${user.asMention}.")
    }

    @SubCommand(
            name = "edit",
            permission = "commands.respects.edit",
            globalCooldown = 0.0
    )
    fun scoreEdit(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty() || context.args.size < 2) {
            context("* Usage: !rep edit <mention> <amount>")
            return
        }
        val user = context.message.mentionedUsers.first()
        reputation[user].transaction(ReputationTransaction("Edited by moderator.", (context.args[1].parseDouble() ?: run {
            context("* Amount specified '${context.args[1]}' is not a valid value.")
            return
        }) - reputation[user].total), context.channel, context.message.guild.getMember(user))
        context(context.author, "Set scores of ${user.asMention} to ${reputation[user].total}.")
    }

    @SubCommand(
            name = "set",
            permission = "commands.respects.set",
            globalCooldown = 0.0
    )
    fun scoreSet(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty() || context.args.size < 2) {
            context("* Usage: !rep set <mention> <amount>")
            return
        }
        val user = context.message.mentionedUsers.first()
        reputation[user].transaction(ReputationTransaction("Edited by moderator.", (reputation[user].total) - (context.args[1].parseDouble() ?: run {
            context("* Amount specified '${context.args[1]}' is not a valid value.")
            return
        }) - reputation[user].total), context.channel, context.message.guild.getMember(user))
        context(context.author, "Set scores of ${user.asMention} to ${reputation[user].total}.")
    }

    @SubCommand(
            name = "leaderboard",
            aliases = arrayOf("repboard", "lboard", "board", "reputationboard", "rtop", "top"),
            permission = "commands.reputation.leaderboard",
            globalCooldown = 30.0
    )
    fun repboard(context: SubCommandContext) {
        val map = reputation.readRawData()
        var top10 = mutableListOf<Member>()
        val amountPositions = mutableListOf<Double>()

        map.forEach { key, value ->
            val member = context.message.guild.getMember(bot.jda.getUserById(key))
            if (member != null) {
                top10.add(member)
                amountPositions.add(value.total)
            }
        }

        Collections.sort(amountPositions)
        Collections.reverse(amountPositions)
        top10 = top10.subList(0, Math.min(top10.size, 10))
        Collections.sort(top10, { second, first -> ((map[first.user.idLong]!!.total) - (map[second.user.idLong]!!.total)).toInt() })
        if (top10.size > 10) {
            val back = mutableListOf<Member>()
            back.addAll(top10.subList(0, 10))
            top10.clear()
            top10.addAll(back)
        }

        val positionStr = StringBuilder()
        val nameStr = StringBuilder()
        val reputationStr = StringBuilder()

        top10.forEach {
            val value = map[it.user.idLong]!!.total

            positionStr.append("#${amountPositions.indexOf(value) + 1}\n")
            nameStr.append("${(it.effectiveName).limit(16)}\n")
            reputationStr.append("${value.toInt()}\n")
        }

        val member = context.message.member
        if(map.containsKey(member.user.idLong) && !top10.contains(member)) {
            val value = map[member.user.idLong]!!.total

            positionStr.append("...\n#${amountPositions.indexOf(value) + 1}")
            nameStr.append("...\n${(member.effectiveName).limit(16)}")
            reputationStr.append("...\n${value.toInt()}")
        }

        context(embed().apply {
            setColor(Color(0x5ea81e))

            addField("Position", positionStr.toString(), true)
            addField("Name", nameStr.toString(), true)
            addField("Reputation", reputationStr.toString(), true)
        })
    }

    fun transaction(guild: Guild, channel: MessageChannel, from: User, to: User, jda: JDA, message: String,
                    amount: (originTotal: Double, targetTotal: Double) -> Double) {
        if (to == from || to == jda.selfUser) {
            channel.sendMessage("* Invalid target.").queue()
            return
        }

        val targetValue = reputation[to]

        val transactionAmount = amount(reputation[from].total, targetValue.total)
        targetValue.transaction(ReputationTransaction("${guild.getMember(from).effectiveName} $message", transactionAmount),
                channel, guild.getMember(to))
        reputation[to] = targetValue
    }
}
