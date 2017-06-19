package com.coalesce.bot.commands.executors

import com.coalesce.bot.COALESCE_GUILD
import com.coalesce.bot.commands.*
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import java.time.OffsetDateTime

class Purge {
    val MAX_BULK_SIZE = 100

    @RootCommand(
            name = "purge",
            permission = "commands.purge",
            type = CommandType.ADMINISTRATION,
            description = "Purge messages in chat"
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            throw ArgsException(arrayOf(
                    "**Usage:**",
                    "`!purge msg <id>` Deletes message based on its id",
                    "`!purge user <@user> [optional : amount]` Deletes amount messages from user specified"
            ).joinToString(separator = "\n"))
        }
    }

    @SubCommand(
            name = "message",
            permission = "command.purge.message",
            aliases = arrayOf("msg")
    )
    fun message(context: SubCommandContext) {
        if (context.args.isEmpty()) {
            context.mention("Specify a message id to delete")
            return
        }
        context.jda.getGuildById(COALESCE_GUILD).textChannels
                .forEach {
                    val message = it.getMessageById(context.args[0])?: run { context("Invalid message id!"); return@forEach }
                    message.queue {
                        it.delete().queue()
                        context("Message with id: '${context.args[0]}' successfully removed!")
                    }
                }
    }

    @SubCommand(
            name = "user",
            permission = "command.purge.user"
    )
    fun user(context: SubCommandContext) {
        if (context.args.isEmpty()) {
            context.mention("Usage: `!purge user <@user> [optional amount]`")
            return
        }

        val member: User
        if (context.message.mentionedUsers.isEmpty()) {
            context.mention("You have to specify an user!")
            return
        } else member = context.message.mentionedUsers.first()

        var amount = context.args[1].toIntOrNull() ?: 1
        amount = Math.min(MAX_BULK_SIZE, amount)

        val time = OffsetDateTime.now().minusWeeks(2) //We can't remove messages older than 2 weeks

        purge({ it.author != null && it.author.idLong == member.idLong && !it.creationTime.isAfter(time) }, context.channel, amount)
    }

    fun purge(check: (Message) -> Boolean, channel: MessageChannel, amount: Int) {
        channel.history.retrievePast(MAX_BULK_SIZE).queue {
            if (it.isEmpty()) {
                channel.sendMessage("* No history found!").queue()
                return@queue
            }
            var removed = 0

            it.forEach {
                if (!check(it) || removed >= amount) return@forEach
                it.delete().queue()
                ++ removed
            }

            channel.sendMessage("Removed $removed/$amount messages requested!").queue()
        }

    }
}