package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.User
import java.awt.Color

class Lmgtfy {
    @RootCommand(
            name = "lmgtfy",
            aliases = arrayOf("justgoogleretard", "googlethat"),
            description = "Meme command (Have you tried with Google yet? Let me do it for you)",
            permission = "commands.lmgtfy",
            usage = "<user : optional> <phrase>",
            globalCooldown = 5.0,
            type = CommandType.FUN
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, "Please specify a phrase to print!")
            return
        }
        val arguments: Array<String>
        var user: User? = context.message.mentionedUsers.firstOrNull()
        if (user == null) {
            user = context.author
            arguments = context.args
        } else {
            arguments = context.args.copyOfRange(1, context.args.size)
            if (arguments.size <= 0) {
                context(context.author, "Please specify a phrase!")
                return
            }
        }

        val phrase = arguments.joinToString(separator = "+")
        context.channel.sendMessage(
                EmbedBuilder().apply {
                    setColor(Color.ORANGE)
                    setTitle("Have you tried Googling it?", "http://lmgtfy.com/?q=$phrase")
                    setDescription("Click the title for more details")
                }.setAuthor(user.name, null, user.avatarUrl).build()
        ).queue()
    }
}
