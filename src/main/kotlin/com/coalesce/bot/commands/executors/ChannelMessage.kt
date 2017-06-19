package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.ArgsException
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class ChannelMessage {
    @RootCommand(
            name = "messagechannel",
            permission = "commands.messagechannel",
            aliases = arrayOf("sendch", "msgch"),
            type = CommandType.DEBUG,
            description = "Message a specific channel"
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            throw ArgsException("Usage: `!sendch <channel> <text>`")
        }

        val channel = context.jda.getTextChannelById(context.args[0]) ?:
                context.jda.getTextChannelsByName(context.args[0], true)[0] ?: throw ArgsException("No channel found by your query!")
        channel.sendMessage(context.args.copyOfRange(1, context.args.size).joinToString(separator = " ")).queue()
        context.mention("Successfully sent message to ${channel.asMention}")
    }
}