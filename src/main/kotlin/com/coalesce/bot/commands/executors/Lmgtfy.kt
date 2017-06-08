package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.User
import java.util.concurrent.TimeUnit

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

        if (context.message.mentionsEveryone() || context.message.mentionedUsers.isNotEmpty() || context.message.mentionedRoles.isNotEmpty()) {
            context(context.author, "You can't tag roles, users or everyone.")
            return
        }
        val phrase = context.args.joinToString(separator = "+")
        context.channel.sendMessage(
                EmbedBuilder().apply {
                    setTitle("Have you tried Googling it?", "http://lmgtfy.com/?q=$phrase")
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setDescription("Click the title for more details")
                }.build()
        ).queue{ it.delete().queueAfter(25, TimeUnit.SECONDS) }
    }
}
