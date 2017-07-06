package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables

@Command("Coinflip", "coin flip")
class CoinFlip: Embeddables {
    @CommandAlias("Flip a coin")
    fun eightBall(context: CommandContext) {
        context("You flipped: ${if (Math.random() < 0.5) "Heads <:heads:324331949961248778>!" else "Tails <:tails:324331963072643073>!"}")
    }
}