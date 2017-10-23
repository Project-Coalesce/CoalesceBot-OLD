package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables

@Command("Coinflip", "coin flip")
class CoinFlip: Embeddables {
    @CommandAlias("Flip a coin")
    fun eightBall(context: CommandContext) {
        context("You flipped: ${if (Math.random() < 0.5) "Heads <:heads:371848960282853387>!" else "Tails <:tails:371848970713956366>!"}")
    }
}