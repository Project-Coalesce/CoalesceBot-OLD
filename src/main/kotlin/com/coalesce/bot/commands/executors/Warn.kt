package com.coalesce.bot.commands.executors

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

class Warn @Inject constructor(val bot: Main, val manager: PunishmentManager) {
    @RootCommand(
            name = "Warn",
            permission = "commands.warn",
            aliases = arrayOf("warning"),
            type = CommandType.ADMINISTRATION,
            description = "Issues a warning on said users record."
    )
    fun execute(context: RootCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            throw ArgsException("You must mention an user to perform this punishment.")
        }
        val user = context.message.mentionedUsers.first()
        var description: String? = null
        if (context.args.size > 1) {
            val desc = StringBuilder()
            Arrays.asList(context.args).subList(1, context.args.size).forEach { desc.append(it).append(' ') }
            description = desc.toString().trim()
        }

        val punishment = Punishment(bot, Reason.GENERAL_WARNING, user, context.author, description, null, (context.channel as TextChannel).guild)
        punishment.doActUpon(manager[user], context.channel)
        manager[user] = punishment
    }
}