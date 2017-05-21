package com.coalesce.bot.permissions

import com.coalesce.bot.utilities.hashTableOf
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import java.util.*
import java.util.stream.Collectors

class RankManager internal constructor(jda: JDA) {
    val ranks = hashTableOf<Long, WrappedRole>()
    val users = hashTableOf<Long, WrappedUser>()

    init {
        jda.guilds.map { it.members }.forEach { it.forEach { users.put(it.user.idLong, WrappedUser(it.user)) } }
    }

    fun getPermissions(member: Member): Map<String, Boolean> {
        val user = users.filter { it == member.user }.values.firstOrNull() ?: return mapOf()
        val applicablePerms = mutableMapOf<String, Boolean>()

        ranks.entries.stream()
                .filter { member.roles.contains(it.value.role) }
                .sorted().collect(Collectors.toList()).reversed().stream().forEachOrdered {applicablePerms.putAll(it.value.permissions) }
        applicablePerms.putAll(user.permissions[member.guild] ?: return applicablePerms)

        return applicablePerms
    }

    fun hasPermission(member: Member, vararg permissions: String): Boolean {
        if (permissions.isEmpty()) {
            return true
        }
        if (member.guild.owner == member) {
            return true
        }
        if (member.roles.stream().anyMatch { it.hasPermission(Permission.ADMINISTRATOR) }) {
            return true
        }
        val permsMap = getPermissions(member)
        val perms = permsMap.entries.map { it.key }

        return Arrays.stream(permissions).allMatch { perms.contains(it) }
    }
}