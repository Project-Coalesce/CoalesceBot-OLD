package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.permissions.WrappedUser
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Member
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

        if (context.args.size == 1) {
            //Global permissions
            val perm = context.args[0]
            if (perm == ";") {
                context("That permission is reserved.")
                return
            }

            perms[perm] = !(perms.global[perm] ?: false)
            context("${if (perms.global[perm]!!) "Added" else "Removed"} $perm to being accessed globally.")
        } else {
            val perm = context.args[1]
            if (perm == ";") {
                context("That permission is reserved.")
                return
            }
            val value: Boolean?
            if (context.args.size >= 3) {
                value = context.args[2].toBoolean()
            } else {
                value = null
            }

            if (context.message.mentionedUsers.isNotEmpty()) {
                perms[context.message.author] = Triple(context.message.guild, perm, value)

                val member = context.message.guild.getMember(context.message.author)
                context("${if(perms[Triple(context.message.author, context.message.guild, perm)]) "Added" else "Removed"} $perm for ${member.asMention}")
            } else if (context.message.mentionedRoles.isNotEmpty()) {
                val role = context.message.mentionedRoles.first()
                perms[role] = perm to value
                context("${if(perms[role to perm]) "Added" else "Removed"} $perm for ${role.name}")
            } else if (context.args.isNotEmpty()) {
                val roles = bot.jda.getRolesByName(context.args.first(), true)

                if (roles.isEmpty()) {
                    context("* The entered argument for what the change permission does not exist.")
                    return
                }

                val role = roles.first()
                perms[role] = perm to value
                context("${if(perms[role to perm]) "Added" else "Removed"} $perm for ${role.name}")
            }
        }
    }
}