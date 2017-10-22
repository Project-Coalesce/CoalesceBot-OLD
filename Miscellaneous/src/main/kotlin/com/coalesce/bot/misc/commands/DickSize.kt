package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables
import net.dv8tion.jda.core.entities.User
import java.util.*

@Command("DickSize", "dicc dick penis penissize")
class DickSize: Embeddables {
    @CommandAlias("Take a look at someone's (Or your own) dick")
    fun execute(context: CommandContext, user: User = context.author) {
        val dickPart = StringBuilder().apply { (0..Random(user.idLong).nextInt(30)).forEach { append("=") } }.toString()
        context("${user.asMention}'s dick: 8${dickPart}D")
    }
}
