package com.coalesce.bot.administration.commands

import com.coalesce.bot.command.*
import com.sun.jna.Memory.purge
import net.dv8tion.jda.core.entities.*

@Command("Permission", "permissions perm perms")
class Permission {
    @CommandAlias("Toggle permission from being accessed globally")
    fun permission(context: CommandContext, perm: String) {
        val handler = PermHandler[context.guild]
        handler += perm
        context(if (handler[perm]) "The permission '$perm' will now be accessible by anyone." else
            "The permission '$perm' will now require additional overrides to be accessible.")
    }

    @CommandAlias("Toggle permission from being accessed by an user")
    fun permission(context: CommandContext, user: User, perm: String) {
        val handler = PermHandler[context.guild][user]
        handler += perm
        context(if (handler[perm]) "The permission '$perm' will now be accessible by ${user.asMention}." else
            "The permission '$perm' will no longer be accessible by ${user.asMention}.")
    }

    @CommandAlias("Toggle permission from being accessed by an user")
    fun permission(context: CommandContext, role: Role, perm: String) {
        val handler = PermHandler[context.guild][role]
        handler += perm
        context(if (handler[perm]) "The permission '$perm' will now be accessible by any user with role ${role.name}." else
            "The permission '$perm' will no longer be accessible by ${role.name}.")
    }
}