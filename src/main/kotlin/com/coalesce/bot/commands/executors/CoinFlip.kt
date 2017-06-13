package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class CoinFlip {

    @RootCommand(
            name = "CoinFlip",
            permission = "commands.coinflip",
            type = CommandType.FUN,
            description = "Flip a coin to see who's more lucky!",
            globalCooldown = 5.0,
            aliases = arrayOf("flip", "coin")
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "You flipped: ${if (Math.random() < 0.5) "Heads!" else "Tails!"}")
    }
}