package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class Broadcast {

    @RootCommand(
            name = "send",
            permission = "command.broadcast",
            type = CommandType.DEBUG,
            description = "Broadcast message into all guilds in their public channel"
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, ": Specify a message to broadcast!")
            return
        }
        val broadcastMessage = context.args.joinToString(separator = " ")
        context.jda.guilds.forEach { it.publicChannel.sendMessage("__***BROADCAST!***__\n$broadcastMessage") }
    }
}