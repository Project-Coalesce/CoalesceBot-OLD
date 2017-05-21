package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.permissions.WrappedRole
import com.coalesce.bot.permissions.WrappedUser
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User

class Permission @Inject constructor(val bot: Main) {
    private val perms = bot.listener.perms

    @RootCommand(
            name = "Permission",
            type = CommandType.ADMINISTRATION,
            permission = "commands.permissions",
            description = "Allows for changing permissions. Roles with 'Administrator' permissions and guild creators have all permissions.",
            cooldownSubs = false,
            aliases = arrayOf("perms", "permissions", "perm")
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context("* Invalid Syntax.")
            return
        }

        val perm = context.args[1]
        val value: Boolean?

        if (context.args.size >= 3) {
            value = context.args[2].toBoolean()
        } else {
            value = null
        }

        if (context.args.size == 1) {
            if (perms.global.contains(perm)) {
                perms.global.remove(perm)
                context("Removed $perm from being accessed globally.")
            } else {
                perms.global.add(perm)
                context("Added $perm to being accessed globally.")
            }
        } else if (context.message.mentionedUsers.isNotEmpty()) {
            changePermissionForUser(context.message.guild.getMember(context.message.author), perm, value, context)
        } else if (context.message.mentionedRoles.isNotEmpty()) {
            changePermissionForRole(context.message.mentionedRoles.first(), perm, value, context)
        } else if (context.args.isNotEmpty()) {
            val roles = bot.jda.getRolesByName(context.args.first(), true)

            if (roles.isEmpty()) {
                context("* The entered argument for what the change permission does not exist.")
                return
            }

            val role = roles.first()
            changePermissionForRole(role, perm, value, context)
        }
    }

    fun changePermissionForRole(role: Role, perm: String, value: Boolean?, context: RootCommandContext) {
        val wrappedRole = findWrappedRole(role)
        wrappedRole.permissions[perm] = value ?: !(wrappedRole.permissions[perm] ?: false)
        context("${if(wrappedRole.permissions[perm]!!) "Added" else "Removed"} $perm for ${role.name}")
    }

    fun findWrappedRole(role: Role): WrappedRole {
        val wrappedRole = perms.ranks[role.idLong]
        if (wrappedRole == null) {
            perms.ranks[role.idLong] = WrappedRole(role)
            return perms.ranks[role.idLong]!!
        }
        return wrappedRole
    }

    fun changePermissionForUser(member: Member, perm: String, value: Boolean?, context: RootCommandContext) {
        val guildMap = findGuildMap(member)
        guildMap[perm] = value ?: !(guildMap[perm] ?: false)
        context("${if(guildMap[perm]!!) "Added" else "Removed"} $perm for ${member.effectiveName}")
    }

    fun findGuildMap(member: Member): MutableMap<String, Boolean> {
        val wrappedUser = findWrappedUser(member.user)
        val guildMap = wrappedUser.permissions[member.guild]
        if (guildMap == null) {
            wrappedUser.permissions[member.guild] = mutableMapOf()
            return wrappedUser.permissions[member.guild]!!
        }
        return guildMap
    }

    fun findWrappedUser(user: User): WrappedUser {
        val wrappedUser = perms.users[user.idLong]
        if (wrappedUser == null) {
            perms.users[user.idLong] = WrappedUser(user)
            return perms.users[user.idLong]!!
        }
        return wrappedUser
    }
}