package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.Embeddables
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
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
            context(context.author, "Specify a message to broadcast!")
            return
        }
        val message = embed().setTitle("Broadcast", null)
                .setAuthor(context.message.author.name, null, context.message.author.avatarUrl)
                .setDescription(context.args.joinToString(separator = " "))
                .setColor(Color(0x5ea81e))
                .setFooter("This is a global broadcast.", null)

        context.jda.guilds.forEach {
            it.publicChannel.sendMessage(message.build()).queue()
        }
    }
}