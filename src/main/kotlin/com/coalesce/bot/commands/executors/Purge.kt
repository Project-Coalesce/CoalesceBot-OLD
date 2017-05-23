package com.coalesce.bot.commands.executors

import com.coalesce.bot.COALESCE_GUILD
import com.coalesce.bot.commands.*
import com.google.inject.Inject
import java.util.concurrent.ExecutorService

const val MAX_DELETE_COUNT = 2500
const val MAX_BULK_SIZE = 100


class Purge @Inject constructor(val executorService: ExecutorService) {

    @RootCommand(
            name = "purge",
            permission = "command.purge",
            type = CommandType.DEBUG,
            description = "Purge messages in chat"
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, "__**Usage:**__\n" +
                    "`!purge msg <id>` Deletes message based on its id" +
                    "`!purge user <userId> [optional : amount]` Deletes amount messages from user specified")
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
                    message.queue { it.delete().queue() }
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

        if (context.args.isEmpty() || context.args.size < 1) {
            mention("Usage: !purge user <user> <channel> [optional amount]")
            return
        }

        val guild = context.jda.getGuildById(COALESCE_GUILD)

        val member = guild.getMemberById(context.args[0]) ?: run { mention("No user could be found with that id!"); return }
        val channel = guild.getTextChannelById(context.args[1]) ?: run { mention("No channel could be found with that id!"); return }

        var amount: Int = 1

        val provided = context.args[2].toIntOrNull()
        if (provided != null) amount = Math.min(MAX_DELETE_COUNT, provided)

        //TODO see how I can improve this
        executorService.submit {
            while (amount > 0) {
                val history = channel.history.retrievePast(MAX_BULK_SIZE).complete()
                if (history.isEmpty()) break
                history.forEach {
                    val author = it.author
                    if (author != null && author.idLong == member.user.idLong) {
                        it.delete().queue()
                        amount--
                    }
                }
            }
        }
    }
}