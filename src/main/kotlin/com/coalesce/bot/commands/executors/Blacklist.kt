package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.ArgsException
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.subList
import com.google.inject.Inject

class Blacklist @Inject constructor(val bot: Main) {
    @RootCommand(
            name = "Blacklist",
            permission = "commands.blacklist",
            type = CommandType.ADMINISTRATION,
            description = "Ban/Unban from bot",
            aliases = arrayOf("ban")
    )
    fun execute(context: RootCommandContext) {
        if (context.message.mentionedUsers.size < 1) {
            throw ArgsException("You must tag someone.")
        }
        val user = context.message.mentionedUsers.first()
        if (bot.listener.isBlacklisted(user)) {
            bot.listener.unblacklist(user)
            context(context.author, "Un-blacklisted ${user.asMention} from bot.")
        } else {
            var reason = context.args.toList().subList(1).joinToString(separator = " ")
            if (reason.isEmpty()) reason = "Not Given."
            bot.listener.blacklist(user, reason)
            context(context.author, "Blacklisted ${user.asMention} from bot for $reason.")
        }
    }
}