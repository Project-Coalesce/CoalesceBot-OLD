package com.coalesce.permissions

import com.coalesce.Constants
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import java.io.File

data class WrappedRole(val role: Role, val permissions: MutableMap<String, Boolean> = mutableMapOf())

data class WrappedUser(val member: Member, val permissions: MutableMap<String, Boolean> = mutableMapOf()) {
    init {
        val guildDir = File(Constants.DATA_DIRECTORY, member.guild.id)
        if (guildDir.exists()) {
            val userFile = File(guildDir, member.user.id)
            if (userFile.exists()) {
                userFile.bufferedReader().use {
                    // TODO: Load permissions from json's Map<String permission, Boolean whether or not its enabled>
                }
            }
        }
    }
}