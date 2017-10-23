package com.coalesce.bot.currencies.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext

@Command("Experience", "exp xp")
class Experience {
    @CommandAlias("Shows your current experience and the needed to level up")
    fun execute(context: CommandContext) {
        val xp = context.main.experienceCachedDataManager[context.author.idLong]
        val level = ((xp - 30) / 33.5).toInt()
        val xpInLevel = xp - (30 + (level) * 3)
        val nextLevel = 30 + (level + 1) * 3
        val bar = "╡${StringBuilder().apply { (1..nextLevel).forEach { if (it > xp) append("܅܅") else append("═") } }}╞"
        context("**Level $level**\n$bar\n$xpInLevel/$nextLevel")
    }
}