package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.google.inject.Inject
import java.util.*

class Kick(@Inject val bot: Main) {
    @RootCommand(
            name = "Kick",
            permission = "commands.kick",
            type = CommandType.ADMINISTRATION,
            description = "Kicks the said user out of the server."
    )
    fun execute(context: RootCommandContext) {
        // TODO: Support several guilds.
        if (context.message.guild.idLong == 268187052753944576L &&
                !context.message.guild.getMember(context.author).roles.contains(context.jda.getRoleById("268239031467376640"))) {
            context.send(context.author, "You're not permitted to perform this command.")
            return
        }
        if (context.message.mentionedUsers.isEmpty()) {
            context.send("You need to mention a user to perform this command.")
            return
        }
        val user = context.message.mentionedUsers.first()
        var description: String? = null
        if (context.args.size > 1) {
            val desc = StringBuilder()
            Arrays.asList(context.args).subList(1, context.args.size).forEach { desc.append(it).append(' ') }
            description = desc.toString().trim()
        }
        // TODO: Private message the description to the user meanwhile broadcast it in the public channel of the guild.

        context.message.guild.controller.kick(user.id)
    }
}