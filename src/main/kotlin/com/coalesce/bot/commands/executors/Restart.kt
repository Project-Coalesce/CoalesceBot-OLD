package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class Restart {
    @RootCommand(
            name = "Restart",
            description = "Restarts the bot.",
            permission = "commands.restart",
            type = CommandType.DEBUG
    )
    fun execute(context: RootCommandContext) {
        context("Goodbye, cruel world.") {
            System.exit(-1)
        }
    }
}