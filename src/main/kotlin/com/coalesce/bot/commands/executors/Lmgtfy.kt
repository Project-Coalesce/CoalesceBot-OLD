package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import net.dv8tion.jda.core.entities.User

class Lmgtfy {
    @RootCommand(
            name = "lmgtfy",
            aliases = arrayOf("justgoogleretard", "googlethat"),
            description = "Meme command (Have you tried with Google yet? Let me do it for you)",
            permission = "command.lmgtfy",
            usage = "<user : optional> <phrase>",
            globalCooldown = 5.0,
            type = CommandType.INFORMATION
    )
    fun execute(context: RootCommandContext) {
        fun printError() {
            context(context.author, "Please specify a phrase to print!")
        }

        if (context.args.isEmpty()) {
            printError()
            return
        }

        var arguments: Array<String>
        var member: User? = context.message.mentionedUsers.firstOrNull()
        if (member == null) {
            member = context.author
            arguments = context.args
        } else {
            if (context.args.size < 1) {
                printError()
                return
            }
            arguments = context.args.copyOfRange(1, context.args.size)
        }

        val phrase = arguments.joinToString(separator = "+")
        context(member, "Have you tried Googling it? <http://lmgtfy.com/?q=$phrase>")
    }
}
