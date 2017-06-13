package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.*
import com.coalesce.bot.reputation.ReputationManager
import com.coalesce.bot.reputation.ReputationTransaction
import com.coalesce.bot.reputation.ReputationValue
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.awt.Color

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
                rep.transactions.joinToString { "**${if (it.amount >= 0) "+" else ""}${it.amount.toInt()}**: ${it.message}\n" }
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
