package com.coalesce.bot.commands.executors

import com.coalesce.bot.COALESCE_GUILD
import com.coalesce.bot.commands.*
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import java.time.OffsetDateTime
import java.util.function.Predicate

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
            context(context.author, "\n**Usage:**\n" +
                    "`!purge msg <id>` Deletes message based on its id\n" +
                    "`!purge user <user> <channel> [optional : amount]` Deletes amount messages from user specified\n" +
                    "`!purge search <search query>` Deletes the message with the search query specified (Based on search feature in discord)")
        }
    }

    @SubCommand(
            name = "message",
            permission = "command.purge.message",
            aliases = arrayOf("msg")
    )
    fun message(context: SubCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, "Specify a message id to delete")
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
        fun mention(text: String) {
            context(context.author, text)
        }

        if (context.args.isEmpty()) {
            mention("Usage: `!purge user <user> <channel> [optional amount]`")
            return
        }

        val guild = context.jda.getGuildById(COALESCE_GUILD)

        val member = guild.getMembersByName(context.args[0], true)[0] ?:
                guild.getMemberById(context.args[0]) ?:
                run { mention("No user could be found with that name/id!"); return }
        val channel = guild.getTextChannelsByName(context.args[0], true)[0] ?:
                guild.getTextChannelById(context.args[0]) ?:
                run { mention("No channel could be found with that name/id!"); return }

        var amount = context.args[2].toIntOrNull() ?: 1
        amount = Math.min(MAX_BULK_SIZE, amount)

        val time = OffsetDateTime.now().minusWeeks(2) //We can't remove messages older than 2 weeks

        purge({ it.author != null && it.author.idLong == member.user.idLong && !it.creationTime.isAfter(time) }, channel, amount)
    }

    fun purge(check: (Message) -> Boolean, channel: TextChannel, amount: Int) {
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