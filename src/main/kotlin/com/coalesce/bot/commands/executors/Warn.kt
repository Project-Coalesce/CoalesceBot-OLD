package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.Reason
import com.google.inject.Inject
import java.util.*

class Warn @Inject constructor(val bot: Main, val manager: PunishmentManager) {
    @RootCommand(
            name = "Warn",
            permission = "commands.warn",
            aliases = arrayOf("warning"),
            type = CommandType.ADMINISTRATION,
            description = "Issues a warning on said users record."
    )
    fun execute(context: RootCommandContext) {
        // TODO: Support several guilds.
        if (context.message.guild.idLong == 268187052753944576L &&
                !context.message.guild.getMember(context.author).roles.contains(context.jda.getRoleById("268239031467376640"))) {
            context(context.author, "You're not permitted to perform this command.")
            return
        }
        if (context.message.mentionedUsers.isEmpty()) {
            context("You need to mention a user to perform this command.")
            return
        }
        val user = context.message.mentionedUsers.first()
        var description: String? = null
        if (context.args.size > 1) {
            val desc = StringBuilder()
            Arrays.asList(context.args).subList(1, context.args.size).forEach { desc.append(it).append(' ') }
            description = desc.toString().trim()
        }

        val punishment = Punishment(bot, Reason.GENERAL_WARNING, user, context.author, description, null)
        punishment.doActUpon(manager[user], context.channel)
        manager[user] = punishment
    }
}