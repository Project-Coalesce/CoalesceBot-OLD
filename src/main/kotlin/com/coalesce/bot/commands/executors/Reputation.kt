package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.*
import com.coalesce.bot.reputation.DownvoteMilestone
import com.coalesce.bot.reputation.ReputationManager
import com.coalesce.bot.reputation.ReputationTransaction
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class Reputation @Inject constructor(val bot: Main, val reputation: ReputationManager) {
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
        val rep = reputation[context.message.author]

        val transactionsString = StringBuilder()
        if (rep.transactions.isEmpty()) transactionsString.append("None.")
        else rep.transactions.forEach {
            transactionsString.append("**${if (it.amount >= 0) "+" else ""}${it.amount.toInt()}**: ${it.message}\n")
        }

        context.send(EmbedBuilder()
                .setTitle("You have ${rep.total.toInt()} reputation.", null)
                .addField("Recent", transactionsString.toString(), false))
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

    @SubCommand(
            name = "thank",
            permission = "commands.reputation.thanks",
            aliases = arrayOf("thanks", "thankyou", "softdonate"),
            globalCooldown = 0.0,
            userCooldown = 360.0
    )
    fun thank(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            context.send("* You need to mention someone you wish to thank.")
            return
        }

        doThank(context.message.guild, context.message.channel, context.message.author, context.message.mentionedUsers.first(), context.jda)
    }

    @SubCommand(
            name = "unrate",
            permission = "commands.reputation.unrate",
            aliases = arrayOf("dislike", "downrate", "unrate"),
            globalCooldown = 0.0,
            userCooldown = 720.0
    )
    fun unrate(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            context.send("* You need to mention someone you wish to unrate.")
            return
        }

        doUnrate(context.message.guild, context.message.channel, context.message.author, context.message.mentionedUsers.first(), context.jda)
    }

    @JDAListener
    fun react(event: MessageReactionAddEvent, context: EventContext) {
        System.out.println(event.reaction.emote.name)
        if (event.reaction.emote.name.contains("✌")) {
            if (context.runChecks(event.user, event.channel!!, 360.0, "thank")) {
                event.channel.getMessageById(event.messageId).queue {
                    doThank(event.guild, event.channel!!, event.user, it.author, event.jda)
                }
            }
        } else if (event.reaction.emote.name.contains("👎")) {
            if (context.runChecks(event.user, event.channel!!, 720.0, "downrate")) {
                event.channel.getMessageById(event.messageId).queue {
                    doUnrate(event.guild, event.channel!!, event.user, it.author, event.jda)
                }
            }
        }
    }

    fun doThank(guild: Guild, channel: MessageChannel, from: User, to: User, jda: JDA) {
        if (from == to) {
            channel.sendMessage("* You can't thank yourself.").queue()
            return
        }
        if (to == jda.selfUser) {
            channel.sendMessage("* You are welcome.").queue()
            return
        }

        val originValue = reputation[from]
        val targetValue = reputation[to]

        val transactionAmount = Math.min((originValue.total / 80.0) + 20.0, 100.0)
        targetValue.transaction(ReputationTransaction("${guild.getMember(from).effectiveName} thanked you", transactionAmount),
                channel, guild.getMember(to))
        reputation[to] = targetValue
    }

    fun doUnrate(guild: Guild, channel: MessageChannel, from: User, to: User, jda: JDA) {
        if (from == to) {
            channel.sendMessage("* You can't unrate yourself.").queue()
            return
        }
        if (to == jda.selfUser) {
            channel.sendMessage("* You are welcome.").queue()
            return
        }

        val originValue = reputation[from]
        val targetValue = reputation[to]

        if (originValue.total < 250) { //TODO improve this, and get milestone from its respective class
            channel.sendMessage("* You need 250 reputation to unrate.").queue()
            return
        }

        targetValue.transaction(ReputationTransaction("${guild.getMember(from).effectiveName} down-rated you", -10.0),
                channel, guild.getMember(to))
        reputation[to] = targetValue
    }
}