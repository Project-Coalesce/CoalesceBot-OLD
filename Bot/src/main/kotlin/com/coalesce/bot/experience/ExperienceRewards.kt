package com.coalesce.bot.experience

import com.coalesce.bot.utilities.allWhere
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User

open class ExperienceReward(val name: String, val achieveMessage: String, val level: Int) {
    fun handleAchieving(channel: TextChannel, user: User): String {
        onAchieve(channel, user)
        return "**You achieved the $name reward!**\n$achieveMessage"
    }
    open fun onAchieve(channel: TextChannel, user: User) {}
}

object ExperienceRewards {
    val rewards = mutableListOf<ExperienceReward>()
    fun checkRewards(level: Int): List<ExperienceReward> = rewards.allWhere { it.level == level }
}