package com.coalesce.bot.reputation

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel

open class ReputationMilestone(val name: String, val messageGotten: String, val messageLost: String, val rep: Int) {
    fun reached(member: Member, channel: MessageChannel) {
        channel.sendMessage("**Congratulations, ${member.asMention}! You have reached the $name milestone!**\n$messageGotten").queue()
        reachHandle(member)
    }

    fun lost(member: Member, channel: MessageChannel) {
        channel.sendMessage("${member.asMention}, you have lost the $name milestone. $messageLost").queue()
        lostHandle(member)
    }

    open fun reachHandle(member: Member) {}
    open fun lostHandle(member: Member) {}
}

class DownvoteMilestone : ReputationMilestone(
        "downvote",
        "You can now rate with 👎 for bad messages. Don't abuse this!",
        "You will no longer get to downvote messages.",
        250
)

class ExtraCommandMilestone : ReputationMilestone(
        "extra commands",
        "You now have access to some extra commands!",
        "You no longer have access to the extra commands.",
        500
)

class RespectedRoleMilestone : ReputationMilestone(
        "respected",
        "You were awarded the respected role!",
        "You lost the respected role.",
        1000
) {
    override fun reachHandle(member: Member) = member.guild.controller.addRolesToMember(member, member.guild.getRoleById("314902034463719424")).queue()
    override fun lostHandle(member: Member)  = member.guild.controller.removeRolesFromMember(member, member.guild.getRoleById("314902034463719424")).queue()
}
