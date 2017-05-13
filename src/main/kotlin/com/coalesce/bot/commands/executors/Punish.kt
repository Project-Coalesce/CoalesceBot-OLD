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

class Punish @Inject constructor(val bot: Main, val manager: PunishmentManager) {
    @RootCommand(
            name = "Punish",
            permission = "commands.punish",
            type = CommandType.ADMINISTRATION,
            description = "Issues a punishment on said users record."
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
        if (context.args.size < 2) {
            context("You need to specify a type for the punishment.")
            return
        }

        var description: String? = null
        if (context.args.size > 2) {
            val desc = StringBuilder()
            Arrays.asList(context.args).subList(2, context.args.size).forEach { desc.append(it + " ") }
            description = desc.toString()
        }

        val reason: Reason
        try {
            reason = Reason.valueOf(context.args[1].toUpperCase())
        } catch (e: Exception) {
            val errorMessages = StringBuilder()
            Reason.values().forEach { errorMessages.append(it.toString() + " (" + it.description + " | Severity " + it.severity + ") ") }

            context("That reason does not exist. Here's a list of valid reasons:\n" + errorMessages.toString())
            return
        }

        val punishment = Punishment(bot, reason, user, context.author, description, null)
        punishment.doActUpon(manager[user], context.channel)
        manager[user] = punishment
    }
}