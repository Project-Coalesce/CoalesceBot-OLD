package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class MessageChannel {

    @RootCommand(
            name = "messagechannel",
            permission = "commands.messagechannel",
            aliases = arrayOf("sendch", "msgch"),
            type = CommandType.DEBUG,
            description = "Message a specific channel"
    )
    fun execute(context: RootCommandContext) {
        fun mention(text: String) {
            context(context.author, text)
        }

        if (context.args.isEmpty()) {
            mention("Usage: !sendch <channel> <text>")
            return
        }

        val channel = context.jda.getTextChannelById(context.args[0]) ?:
                context.jda.getTextChannelsByName(context.args[0], true)[0] ?:
                run { mention("No channel could be found with that name/id!"); return }
        channel.sendMessage(context.args.copyOfRange(1, context.args.size).joinToString(separator = " "))
        mention("Successfully sent message to ${channel.asMention}")
    }
}