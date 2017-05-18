package com.coalesce.bot.commands.executors

import com.coalesce.bot.reputation.ReputationTransaction
import com.coalesce.bot.commands.*
import com.coalesce.bot.reputation.ReputationManager
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent

class Reputation @Inject constructor(val reputation: ReputationManager) {
    @RootCommand(
            name = "reputation",
            type = CommandType.INFORMATION,
            permission = "commands.reputation",
            aliases = arrayOf("rep", "reput"),
            description = "View your reputation.",
            userCooldown = 10.0,
            globalCooldown = 3.0
    )
    fun execute(context: RootCommandContext) {
        val rep = reputation[context.message.author]

        val transactionsString = StringBuilder()
        if (rep.transactions.isEmpty()) transactionsString.append("None.")
        else rep.transactions.forEach {
            transactionsString.append("**${if (it.amount >= 0) "+" else ""}${it.amount}**: ${it.message}\n")
        }

        context.send(EmbedBuilder()
                .setTitle("You have ${rep.total.toInt()} reputation.", null)
                .addField("Recent", transactionsString.toString(), false))
    }

    @SubCommand(
            name = "thank",
            permission = "commands.reputation.thanks",
            aliases = arrayOf("thanks", "thankyou", "softdonate"),
            globalCooldown = 5.0,
            userCooldown = 360.0
    )
    fun thank(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            context.send("* You need to mention someone you wish to thank.")
            return
        }

        doThank(context.message.guild, context.message.channel, context.message.author, context.message.mentionedUsers.first(), context.jda)
    }

    @JDAListener
    fun react(event: MessageReactionAddEvent) {
        if (event.reaction.emote.name == "✌") {
            event.channel.getMessageById(event.messageId).queue {
                doThank(event.guild, event.channel, event.user, it.author, event.jda)
            }
        } else if (event.reaction.emote.name == "👎") {

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

        val transactionAmount = Math.min((originValue.total.toDouble() / 80.0) + 20.0, 100.0)
        targetValue.transaction(ReputationTransaction("${guild.getMember(from).effectiveName} thanked you", transactionAmount),
                channel, guild.getMember(to))
        reputation[to] = targetValue
    }
}