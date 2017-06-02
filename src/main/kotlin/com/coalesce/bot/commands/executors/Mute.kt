package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.punishmentals.Reason
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.TextChannel
import java.util.*

class Mute @Inject constructor(val bot: Main, val manager: PunishmentManager) {
    @RootCommand(
            name = "Mute",
            permission = "commands.mute",
            aliases = arrayOf("silence"),
            type = CommandType.ADMINISTRATION,
            description = "Issues a mute on said users record."
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
            context("You need to specify a time period for the mute to last.")
            return
        }

        var description: String? = null
        if (context.args.size > 2) {
            val desc = StringBuilder()
            Arrays.asList(context.args).subList(2, context.args.size).forEach { desc.append(it + " ") }
            description = desc.toString()
        }

        var time: Calendar? = Calendar.getInstance()
        if (context.args[1].equals("permanent", true) || context.args[1].equals("null", true) || context.args[1].equals("-1", false)) {
            time = null
        } else {
            val arg = context.args[1]
            var timeUnit: Int = Calendar.DAY_OF_MONTH
            val timeAdd = Integer.parseInt(arg.substring(0, arg.length - 1))

            if (arg.endsWith("h")) timeUnit = Calendar.HOUR
            else if (arg.endsWith("m")) timeUnit = Calendar.MINUTE
            else if (arg.endsWith("s")) timeUnit = Calendar.SECOND
            else if (arg.endsWith("w")) timeUnit = Calendar.WEEK_OF_MONTH
            else if (arg.endsWith("M")) timeUnit = Calendar.MONTH

            time?.add(timeUnit, timeAdd)
        }

        val punishment = Punishment(bot, Reason.GENERAL_WARNING, user, context.author, description, time?.timeInMillis, (context.channel as TextChannel).guild)
        punishment.doActUpon(manager[user], context.channel)
        manager[user] = punishment
    }
}