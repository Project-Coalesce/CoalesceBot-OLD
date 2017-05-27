package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import net.dv8tion.jda.core.entities.Game

class Playing {

    @RootCommand(
            name = "playing",
            permission = "commands.playing",
            aliases = arrayOf("setstatus"),
            type = CommandType.DEBUG,
            description = "Change bot game message"
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, "Usage: `!playing <text>`")
            return
        }
        val status = context.args.joinToString(separator = " ")
        context("* Game changed to: $status")
        context.jda.presence.game = Game.of(status)
    }
}