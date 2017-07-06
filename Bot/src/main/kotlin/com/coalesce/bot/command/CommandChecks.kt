package com.coalesce.bot.command

import com.coalesce.bot.dataDirectory
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import java.io.File
import java.util.concurrent.TimeUnit

// Cooldown

class CooldownHandler: Embeddables {
    private val globalCooldown = mutableMapOf<CommandFrameworkClass.CommandInfo, Long>()
    private val userCooldown = mutableMapOf<User, MutableMap<CommandFrameworkClass.CommandInfo, Long>>()

    fun doGlobalCooldown(info: CommandFrameworkClass.CommandInfo) {
        globalCooldown.put(info, System.currentTimeMillis())
        timeOutHandler(info.globalCooldown, TimeUnit.MILLISECONDS) { globalCooldown.remove(info) }
    }

    fun doUserCooldown(user: User, info: CommandFrameworkClass.CommandInfo) {
        userCooldown[user] = (userCooldown[user] ?: mutableMapOf()).apply { put(info, System.currentTimeMillis()) }
        timeOutHandler(info.globalCooldown, TimeUnit.MILLISECONDS) { (userCooldown[user] ?: return@timeOutHandler).remove(info) }
    }

    fun cooldownCheck(context: Context): Boolean {
        val info = context.info
        if (userCooldown.containsKey(context.author) && userCooldown[context.author]!!.containsKey(info)) {
            context(embed().apply {
                embTitle = "Wait before you can run this command again!"
                embDescription = "⏰ Cooldown for: **${(System.currentTimeMillis() - userCooldown[context.author]!![info]!!).formatTimeDiff()}**"
            }, deleteAfter = 8L to TimeUnit.SECONDS)
            return false
        }
        if (globalCooldown.containsKey(info)) {
            context(embed().apply {
                embTitle = "Wait before you can run this command again!"
                embDescription = "⏰ Global Cooldown for: **${(System.currentTimeMillis() - globalCooldown[info]!!).formatTimeDiff()}**"
            }, deleteAfter = 8L to TimeUnit.SECONDS)
            return false
        }

        return true
    }
}

// Permissions

class PermHandler private constructor(private val guildDataFolder: File, private val guild: Guild): Embeddables, Timeout(30L, TimeUnit.MINUTES) {
    companion object {
        private val permHandlers = mutableMapOf<Guild, PermHandler>()

        operator fun get(guild: Guild) = permHandlers[guild] ?: PermHandler(File(dataDirectory, guild.idLong.toString()), guild)
    }

    private val memberOverrides = mutableMapOf<User, MutableList<String>>()
    private val roleOverrides = mutableMapOf<Role, MutableList<String>>()
    private val globalPermissions = mutableListOf<String>()

    private val membersFile = File(guildDataFolder, "members.json")
    private val rolesFile = File(guildDataFolder, "roles.json")
    private val globalFile = File(guildDataFolder, "global.json")

    init {
        if (membersFile.exists()) memberOverrides.putAll(gson.fromJson(membersFile.readText(), memberOverrides.javaClass))
        if (rolesFile.exists()) roleOverrides.putAll(gson.fromJson(rolesFile.readText(), roleOverrides.javaClass))
        if (globalFile.exists()) globalPermissions.addAll(gson.fromJson(globalFile.readText(), Array<String>::class.java))
    }

    override fun timeout() {
        // Ensure everything is saved
        save(membersFile, memberOverrides)
        save(rolesFile, roleOverrides)
        save(globalFile, globalPermissions)

        permHandlers.remove(guild)
    }

    private fun save(file: File, obj: Any) {
        if (!guildDataFolder.exists()) guildDataFolder.mkdirs()
        if (!file.exists()) file.createNewFile()
        file.writeText(gson.toJson(obj))
    }

    operator fun get(user: User) = WrappedUser(user, this)
    operator fun get(role: Role) = WrappedRole(role, this)

    operator fun get(perm: String) = globalPermissions.contains(perm)
    operator fun plusAssign(permission: String) {
        if (globalPermissions.contains(permission)) globalPermissions.remove(permission) else globalPermissions.add(permission)
        save(globalFile, globalPermissions)
    }

    operator fun invoke(user: User, permission: String): Boolean {
        if (guild.getMember(user).run { isOwner || roles.any { it.hasPermission(Permission.ADMINISTRATOR) } }) return true

        val allPermissions = mutableListOf<String>().apply {
            if (memberOverrides.containsKey(user)) addAll(memberOverrides[user]!!)
            guild.getMember(user).roles.forEach { if (roleOverrides.containsKey(it)) addAll(roleOverrides[it]!!) }
            addAll(globalPermissions)
        }

        return allPermissions.contains(permission)
    }

    class WrappedRole internal constructor(private val role: Role, private val handler: PermHandler) {
        operator fun get(perm: String): Boolean {
            return (handler.roleOverrides[role] ?: return false).contains(perm)
        }
        operator fun plusAssign(permission: String) {
            handler.roleOverrides[role] = (handler.roleOverrides[role] ?: mutableListOf()).apply {
                if (contains(permission)) remove(permission) else add(permission)
            }
            handler.save(handler.rolesFile, handler.roleOverrides)
        }
    }

    class WrappedUser internal constructor(private val user: User, private val handler: PermHandler) {
        operator fun get(perm: String): Boolean {
            return (handler.memberOverrides[user] ?: return false).contains(perm)
        }
        operator fun plusAssign(permission: String) {
            handler.memberOverrides[user] = (handler.memberOverrides[user] ?: mutableListOf()).apply {
                if (contains(permission)) remove(permission) else add(permission)
            }
            handler.save(handler.membersFile, handler.memberOverrides)
        }
    }
}

fun permCheck(context: Context): Boolean {
    if (!PermHandler[context.guild](context.author, "Commands.${context.info.name}") && context.channel.idLong != 315934590109745154L) {
        context(EmbedBuilder().apply {
            embTitle = "Access Denied!"
            embDescription = "🚫 You lack permission to run this command."
        }, deleteAfter = 8L to TimeUnit.SECONDS)
        return false
    }

    return true
}
