package com.coalesce.bot.permissions

import com.coalesce.bot.binary.PermissionsMapSerializer
import com.coalesce.bot.dataDirectory
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import java.io.File

data class WrappedRole(val role: Role, val permissions: MutableMap<String, Boolean> = mutableMapOf()) {
    init {
        val guildDir = File(dataDirectory, "Guild_${role.guild.id}")
        if (guildDir.exists()) {
            val roleFile = File(guildDir, "Role_${role.id}.dat")
            if (roleFile.exists()) {
                println("Loaded permissions from existing file for: ${role.name}.")
                val serializer = PermissionsMapSerializer(roleFile)
                permissions.putAll(serializer.read())
            }
        }
    }

    fun save() {
        val guildDir = File(dataDirectory, "Guild_${role.guild.id}")
        if (!guildDir.exists()) {
            guildDir.mkdirs()
        }
        val roleFile = File(guildDir, "Role_${role.id}.dat")
        if (roleFile.exists()) {
            roleFile.delete()
        }
        roleFile.createNewFile()

        val serializer = PermissionsMapSerializer(roleFile)
        serializer.write(permissions)
    }
}

data class WrappedUser(val member: User, val permissions: MutableMap<Guild, MutableMap<String, Boolean>> = mutableMapOf()) {
    init {
        member.mutualGuilds.forEach {
            val guildDir = File(dataDirectory, "Guild_${it.id}")
            if (guildDir.exists()) {
                val userFile = File(guildDir, "Member_${member.id}.dat")
                if (userFile.exists()) {
                    println("Loaded permissions from existing file for: ${member.name}.")
                    val serializer = PermissionsMapSerializer(userFile)
                    permissions[it] = serializer.read()
                }
            }
        }
    }

    fun save() {
        member.mutualGuilds.forEach {
            if (permissions[it] == null) return@forEach

            val guildDir = File(dataDirectory, "Guild_${it.id}")
            if (!guildDir.exists()) {
                guildDir.mkdirs()
            }
            val memberFile = File(guildDir, "Member_${member.id}.dat")
            if (memberFile.exists()) {
                memberFile.delete()
            }
            memberFile.createNewFile()

            val serializer = PermissionsMapSerializer(memberFile)
            serializer.write(permissions[it]!!)
        }
    }
}