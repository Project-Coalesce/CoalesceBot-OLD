package com.coalesce.bot.currencies.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.google.common.base.Strings



@Command("Experience", "exp xp")
class Experience {
    private val totalBars = 50

    @CommandAlias("Shows your current experience and the needed to level up")
    fun execute(context: CommandContext) {
        val manager = context.main.experienceCachedDataManager

        /*val xp = manager.getExp(context.author)
        val level = manager.getLevel(xp)

        val nextLevel = manager.getExpInLevel(level)

        val bar = "╡${getProgressBar(xp, nextLevel)}╞"
        context("**Level $level**\n$bar\n${manager.getExpToLevel(level)} exp for next level!")*/
    }

    fun getProgressBar(current: Int, max: Int): String {
        val percent = current.toFloat() / max
        val progressBars = (totalBars * percent).toInt()

        return Strings.repeat("=", progressBars) + Strings.repeat("-", totalBars - progressBars)
    }
}