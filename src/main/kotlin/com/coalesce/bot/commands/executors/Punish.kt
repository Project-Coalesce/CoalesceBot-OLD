package com.coalesce.bot.commands.executors

import com.coalesce.bot.COALESCE_GUILD
import com.coalesce.bot.Main
import com.coalesce.bot.commands.ArgsException
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.Reason
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.TextChannel
import java.util.*

class Punish @Inject constructor(val bot: Main, val manager: PunishmentManager) {
    @RootCommand(
            name = "Punish",
            permission = "commands.punish",
            type = CommandType.ADMINISTRATION,
            description = "Issues a punishment on said users record."
    )
    fun execute(context: RootCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            throw ArgsException("You must mention an user to perform this punishment.")
            return
        }
        val user = context.message.mentionedUsers.first()
        if (context.args.size < 2) {
            throw ArgsException("You must specify a reason for the punishment.")
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
            throw ArgsException("That reason does not exist. Here's a list of valid reasons:\n" + Reason.values().joinToString(separator = " ") {
                it.toString() + " (" + it.description + " | Severity " + it.severity + ") "
            })
        }

        val punishment = Punishment(bot, reason, user, context.author, description, null, (context.channel as TextChannel).guild)
        punishment.doActUpon(manager[user], context.channel)
        manager[user] = punishment
    }
}