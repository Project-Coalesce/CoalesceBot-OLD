package com.coalesce.bot.permissions

import com.coalesce.bot.binary.PermissionsMapSerializer
import com.coalesce.bot.globalPermissionsFile
import com.coalesce.bot.utilities.hashTableOf
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import java.util.*
import java.util.stream.Collectors

class RankManager internal constructor(jda: JDA) {
    private val serializer = PermissionsMapSerializer(globalPermissionsFile)
    val ranks = hashTableOf<Long, WrappedRole>()
    val users = hashTableOf<Long, WrappedUser>()
    val global = mutableMapOf<String, Boolean>()

    init {
        jda.guilds.map { it.members }.forEach { it.forEach { users.put(it.user.idLong, WrappedUser(it.user)) } }
        if (globalPermissionsFile.exists()) {
            println("Loaded global permissions from existing file.")
            global.putAll(serializer.read())
        }
    }

    operator fun set(permName: String, perm: Boolean?) {
        global[permName] = perm ?: !(global[permName] ?: false)
        saveGlobal()
    }

    operator fun get(permName: String): Boolean {
        return global[permName] ?: false
    }

    operator fun set(member: User, perm: Triple<Guild, String, Boolean?>) {
        val idlong = member.idLong
        if (users[idlong] == null) {
            users[idlong] = WrappedUser(member)
        }
        val user = users[idlong]!!
        if (user.permissions[perm.first] == null) {
            user.permissions[perm.first] = mutableMapOf()
        }
        val guildPermMap = user.permissions[perm.first]!!
        guildPermMap[perm.second] = perm.third ?: !(guildPermMap[perm.second] ?: false)
        user.save()
    }

    operator fun get(perm: Triple<User, Guild, String>): Boolean {
        try {
            return users[perm.first.idLong]!!.permissions[perm.second]!![perm.third] ?: false
        } catch (ex: Exception) {
            return false
        }
    }

    operator fun set(role: Role, perm: Pair<String, Boolean?>) {
        val idlong = role.idLong
        if (ranks[idlong] == null) {
            ranks[idlong] = WrappedRole(role)
        }
        val rank = ranks[idlong]!!
        rank.permissions[perm.first] = perm.second ?: !(rank.permissions[perm.first] ?: false)
        rank.save()
    }

    operator fun get(perm: Pair<Role, String>): Boolean {
        try {
            return ranks[perm.first.idLong]!!.permissions[perm.second] ?: false
        } catch (ex: Exception) {
            return false
        }
    }

    fun saveGlobal() {
        if (globalPermissionsFile.exists()) {
            globalPermissionsFile.delete()
        }
        globalPermissionsFile.createNewFile()

        serializer.write(global)
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
        /*
        if (permissions.isEmpty() || member.guild.owner == member || member.roles.stream().anyMatch { it.hasPermission(Permission.ADMINISTRATOR) }) {
            return true
        }
        */

        val permsMap = getPermissions(member)
        val perms = permsMap.entries.stream().filter { it.value }.map { it.key }.toArray()

        return Arrays.stream(permissions).allMatch { perms.contains(it) || global.contains(it) }
    }
}