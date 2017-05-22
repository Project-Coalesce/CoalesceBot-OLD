package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.*

class Purge {

    //TODO add support for multiple guilds!
    val coalesceGuild = 268187052753944576L

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
        context.jda.getGuildById(coalesceGuild).textChannels
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
        //TODO user purge command
    }
}