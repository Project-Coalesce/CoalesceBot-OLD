package com.coalesce.bot.permissions

import com.coalesce.bot.Main
import com.coalesce.bot.utilities.hashTableOf
import net.dv8tion.jda.core.entities.Member
import java.util.*
import java.util.stream.Collectors

class RankManager internal constructor(bot: Main) {
    private val ranks = hashTableOf<Long, WrappedRole>()
    private val users = hashTableOf<Long, WrappedUser>()

    init {
        bot.jda.guilds.map { it.members }.forEach { it.forEach { users.put(it.user.idLong, WrappedUser(it.user)) } }
    }

    fun getPermissions(member: Member): Map<String, Boolean> {
        val user = users.filter { it == member.user }.values.firstOrNull() ?: return mapOf()
        val applicablePerms = mutableMapOf<String, Boolean>()
        ranks.entries.stream()
                .filter { it -> member.roles.contains(it.value.role) }
                .sorted().collect(Collectors.toList()).reversed().stream().forEachOrdered { it -> applicablePerms.putAll(it.value.permissions) }
        applicablePerms.putAll(user.permissions[member.guild] ?: return applicablePerms)
        return applicablePerms
    }

    fun hasPermission(member: Member, vararg permissions: String): Boolean {
        if (permissions.isEmpty()) {
            return true
        }
        val permsMap = getPermissions(member)
        val perms = permsMap.entries.map { it.key }
        return Arrays.stream(permissions).allMatch { perms.contains(it) }
    }
}