package com.coalesce.bot.command.handlers

import com.coalesce.bot.VERSION
import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.UserCooldown
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import java.awt.Color

@Command("Info", "about botinfo")
@UserCooldown(10L)
class Info: Embeddables {
    @CommandAlias("Gives you info about the bot")
    fun execute(context: CommandContext) =
            context(embed().apply {
                embColor = Color(0xBE58B6)
                embTitle = "CoalesceBot v$VERSION"

                embDescription = arrayOf(
                        "Originally started by Lyxnx & Proximyst, and currently mantained by NahuLD and deprilula28; All members of the " +
                        "Coalesce Team (Learn more: `!coalesce`).",
                        "If you find any bugs, please report them at https://goo.gl/5rGeFJ.",
                        "Abusing the bot's features or an exploit may lead into a blacklist.",
                        "**Type `!help` for a list of commands.**")
                .joinToString(separator = "\n")
            })
}