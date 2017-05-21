package com.coalesce.bot.permissions

import com.coalesce.bot.binary.PermissionsMapSerializer
import com.coalesce.bot.dataDirectory
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import java.io.File

data class WrappedRole(val role: Role, val permissions: MutableMap<String, Boolean> = mutableMapOf())

data class WrappedUser(val member: User, val permissions: MutableMap<Guild, MutableMap<String, Boolean>> = mutableMapOf()) {
    init {
        member.mutualGuilds.forEach {
            val guildDir = File(dataDirectory, "Guild_${it.id}")
            if (guildDir.exists()) {
                val userFile = File(guildDir, "${member.id}.dat")
                if (userFile.exists()) {
                    val serializer = PermissionsMapSerializer(userFile)
                    permissions[it] = serializer.read()
                }
            }
        }
    }
}