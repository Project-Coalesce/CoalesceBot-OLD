package com.coalesce.bot.reputation

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel

abstract class ReputationMilestone(val name: String, val messageGotten: String, val messageLost: String) {
    fun reached(member: Member, channel: MessageChannel) {
        channel.sendMessage("Congratulations, ${member.effectiveName}! You have reached the $name milestone! $messageGotten").queue()
        reachHandle(member)
    }

    fun lost(member: Member, channel: MessageChannel) {
        channel.sendMessage("${member.effectiveName}, you have lost the $name milestone. $messageLost").queue()
        lostHandle(member)
    }

    abstract fun reachHandle(member: Member)
    abstract fun lostHandle(member: Member)
}

class DownvoteMilestone : ReputationMilestone("downvote", "You can now rate with 👎 for bad messages. Don't abuse this!",
        "You will no longer get to downvote messages.") {
    override fun reachHandle(member: Member) {}
    override fun lostHandle(member: Member) {}
}
