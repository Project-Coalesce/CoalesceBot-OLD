package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import net.dv8tion.jda.core.entities.Member

class Lmgtfy {
    @RootCommand(
            name = "lmgtfy",
            aliases = arrayOf("justgoogleretard", "googlethat"),
            description = "Meme command (Have you tried with Google yet? Let me do it for you)",
            permission = "command.lmgtfy",
            usage = "<user> <phrase>",
            globalCooldown = 5.0,
            type = CommandType.INFORMATION
    )
    fun execute(context: RootCommandContext) {
        fun mention(text: String) {
            context(context.author, text)
        }
        val member: Member? = context.message.guild.getMember(context.message.mentionedUsers.firstOrNull())
        if (member == null) {
            mention("You need to specify a user to tag.")
            return
        }
        else if (context.args.isEmpty()) {
            mention("Please specify a phrase to Google.")
        }
        val phrase = context.args.copyOfRange(1, context.args.size).joinToString(separator = "+")
        context(member, "Have you tried Googling it? <http://lmgtfy.com/?q=$phrase>")
    }
}
