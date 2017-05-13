package com.coalesce.bot.permissions

import com.coalesce.bot.dataDirectory
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import java.io.File

data class WrappedRole(val role: Role, val permissions: MutableMap<String, Boolean> = mutableMapOf())

data class WrappedUser(val member: User, val permissions: MutableMap<Guild, MutableMap<String, Boolean>> = mutableMapOf()) {
    init {
        for (guild in member.mutualGuilds) {
            val guildDir = File(dataDirectory, guild.id)
            if (guildDir.exists()) {
                val userFile = File(guildDir, member.id + ".json")
                if (userFile.exists()) {
                    userFile.bufferedReader().use {
                        // TODO: Load permissions from json's Map<Guild, Map<String permission, Boolean whether or not its enabled>>
                    }
                }
            }
        }
    }
}