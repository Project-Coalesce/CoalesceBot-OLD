package com.coalesce.punishments

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import java.util.concurrent.ScheduledExecutorService

class PTimerTask(val guild: Guild, val member: Member, val role: Role, val executorService: ScheduledExecutorService) : Runnable {
    override fun run() {
        guild.controller.removeRolesFromMember(member, role)
        executorService.shutdown()
    }
}