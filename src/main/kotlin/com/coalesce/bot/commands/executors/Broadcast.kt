package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.*
import java.awt.Color

class Broadcast: Embeddables {

    @RootCommand(
            name = "send",
            permission = "commands.broadcast",
            type = CommandType.DEBUG,
            description = "Broadcast message into all guilds in their public channel",
            aliases = arrayOf("broadcast", "bc", "brc")
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            throw ArgsException("Specify a message to broadcast!")
        }
        val message = embed().apply {
            setTitle("Broadcast", null)
            setAuthor(context.message.author.name, null, context.message.author.avatarUrl)
            setDescription(context.args.joinToString(separator = " "))
            setColor(Color(0x5ea81e))
        }

        context.jda.guilds.forEach {
            it.publicChannel.sendMessage(message.build()).queue()
        }
        context("Message broadcast to all guilds!")
    }
}