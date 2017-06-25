package com.coalesce.bot.administration.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.send
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageHistory
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User

@Command("Purge", "clear clean janitor")
class Purge {
    @CommandAlias("Purge a channel")
    fun purgeChannel(context: CommandContext, channel: TextChannel = context.channel, amount: Int = 100) {
        purge(context.channel, amount = amount) { true }
    }

    @CommandAlias("Purge messages from a specific user")
    fun purgeUser(context: CommandContext, user: User, amount: Int = 10) {
        purge(context.channel, amount = amount) { it.author == user }
    }

    @CommandAlias("Purge a specific message")
    fun purgeMessage(context: CommandContext, message: Long) {
        context.channel.getMessageById(message).queue {
            it.delete().queue { context("Message deleted!") }
        }
    }

    private fun purge(channel: TextChannel, history: MessageHistory = channel.history, purged: Int = 0, amount: Int = 100, check: (Message) -> Boolean) {
        var varPurged = purged
        history.retrievePast(Math.min(amount - purged, 100)).queue {
            it.filter(check).forEach {
                it.delete().queue()
                varPurged ++
            }

            if (purged < amount) purge(channel, history, varPurged, amount, check)
            else channel.send("Purged $varPurged messages!")
        }
    }
}