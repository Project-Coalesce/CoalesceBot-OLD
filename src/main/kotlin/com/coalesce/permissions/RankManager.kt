package com.coalesce.permissions

import com.coalesce.Bot
import com.coalesce.utils.reverse
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import java.util.*

class RankManager private constructor() {
    private val ranks = HashMap<Role, Map<String, Boolean>>()
    private val users = HashSet<WrappedUser>()

    init {
        Bot.instance.jda.guilds.stream().map(Guild::getMembers).parallel().forEach({ it.forEach { users.add(WrappedUser(it)) } })
    }

    fun getPermissions(member: Member): Map<String, Boolean> {
        val optional = users.stream().filter { it.member == member }.findFirst()
        if (!optional.isPresent) {
            return HashMap()
        }
        val applicablePerms = HashMap<String, Boolean>()
        val user = optional.get()
        ranks.entries.stream()
                .filter { it -> member.roles.contains(it.key) }
                .sorted().reverse().forEachOrdered { it -> applicablePerms.putAll(it.value) }
        applicablePerms.putAll(user.permissions)
        return applicablePerms
    }

    fun hasPermission(member: Member, vararg permissions: String): Boolean {
        val permsMap = getPermissions(member)
        val perms = permsMap.entries.map { it.key }
        return Arrays.stream(permissions).allMatch { perms.contains(it) }
    }

    companion object {
        val instance = RankManager()
    }
}