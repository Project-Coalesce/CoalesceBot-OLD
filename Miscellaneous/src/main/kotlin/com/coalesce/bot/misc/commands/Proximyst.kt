package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext

@Command("Proximyst", "proxi bae")
class Proximyst {

    @CommandAlias("You will be remembered")
    fun exectue(context: CommandContext) =
            context("Proximyst was a good gal. She helped out a bunch of people, and created this bot." +
                "\nhttp://i.imgur.com/lAkpLgC.png")
}
