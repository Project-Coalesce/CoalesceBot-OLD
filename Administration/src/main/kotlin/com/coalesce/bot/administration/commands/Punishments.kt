package com.coalesce.bot.administration.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.punishmentals.Punishment
import com.coalesce.bot.punishmentals.Reason
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.util.*

@Command("Kick")
class Kick: Embeddables {
    @CommandAlias("Kicks the given user out of the server.")
    fun execute(context: CommandContext, user: User, @VarArg message: String) {
        context.guild.controller.kick(context.guild.getMember(user))

        user.usePCh {
            send(embed().apply {
                embTitle = "You were kicked from ${context.guild.name}."
                embDescription = "Reason: $message"
                embColor = Color(206, 28, 28)
            }.build())
        }
    }
}

@Command("Warn", "warning")
class Warn: Embeddables {
    @CommandAlias("Issues a warning in said users record.")
    fun execute(context: CommandContext, user: User, @VarArg description: String) {
        val punishment = Punishment(context.main, Reason.GENERAL_WARNING, user, context.author, description, null, context.guild)
        punishment.doActUpon(context.main.punishments[user], context.channel)
        context.main.punishments[user] = punishment
    }
}

@Command("Mute", "silence")
class Mute: Embeddables {
    @CommandAlias("Issues a mute in said users record.")
    fun execute(context: CommandContext, user: User, time: Calendar, @VarArg description: String) {
        val punishment = Punishment(context.main, Reason.GENERAL_WARNING, user, context.author, description, time.timeInMillis, context.guild)
        punishment.doActUpon(context.main.punishments[user], context.channel)
        context.main.punishments[user] = punishment
    }
}

@Command("Punish")
class Punish: Embeddables {
    @CommandAlias("Issues a punishment in said users record.")
    fun execute(context: CommandContext, user: User, reason: Reason, time: Calendar? = null, @VarArg description: String) {
        val punishment = Punishment(context.main, reason, user, context.author, description, time?.timeInMillis, context.guild)
        punishment.doActUpon(context.main.punishments[user], context.channel)
        context.main.punishments[user] = punishment
    }
}
