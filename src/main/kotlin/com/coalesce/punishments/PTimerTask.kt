package com.coalesce.punishments

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import java.util.*

class PTimerTask(val guild: Guild, val member: Member, val role: Role) : TimerTask() {
    override fun run() {
        guild.controller.removeRolesFromMember(member, role)
    }
}