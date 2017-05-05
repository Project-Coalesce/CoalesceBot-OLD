package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import java.util.concurrent.TimeUnit

class Respects {
    @RootCommand(
            name = "Respects",
            aliases = arrayOf("f", "nahusdream"),
            description = "Over-engineered meme command (Press F to pay respects)",
            permission = "commands.respects",
            type = CommandType.FUN,
            globalCooldown = 6.0 * 3600.0
    )
    fun execute(context: RootCommandContext) {
        context.send(context.author, "Respects have been paid!") { ifwithDo(canDelete, context.message.guild) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(60, TimeUnit.SECONDS) } } }
        // TODO: Re-implement the leaderboard
    }
}