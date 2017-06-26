package com.coalesce.bot.command.game

import com.coalesce.bot.command.CommandContext
import net.dv8tion.jda.core.entities.User

// Dank Command Framework
class CoGame {
    fun matchfinding(context: CommandContext, target: List<User> = listOf(), targetAmount: Int = if (target.isEmpty()) 1 else target.size,
                     bid: Int) {

    }
}