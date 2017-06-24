package com.coalesce.bot.command.handlers

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables

@Command("Reload", "rl")
class Reload: Embeddables {
    @CommandAlias("Reloads")
    fun execute(context: CommandContext) {
        context("Goodbye, cruel world.") { System.exit(-1) }
    }
}