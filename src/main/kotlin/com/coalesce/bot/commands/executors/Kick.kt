package com.coalesce.bot.commands.executors

import com.coalesce.bot.COALESCE_GUILD
import com.coalesce.bot.Main
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*

class Kick @Inject constructor(val bot: Main) {
    @RootCommand(
            name = "Kick",
            permission = "commands.kick",
            type = CommandType.ADMINISTRATION,
            description = "Kicks the said user out of the server."
    )
    fun execute(context: RootCommandContext) {
        // TODO: Support several guilds.
        if (context.message.guild.idLong == COALESCE_GUILD &&
                !context.message.guild.getMember(context.author).roles.contains(context.jda.getRoleById("268239031467376640"))) {
            context(context.author, "You're not permitted to perform this command.")
            return
        }
        if (context.message.mentionedUsers.isEmpty()) {
            context("You need to mention a user to perform this command.")
            return
        }
        val user = context.message.mentionedUsers.first()
        var description: String? = null
        if (context.args.size > 1) {
            description = context.args.copyOfRange(1, context.args.size).joinToString(separator = " ")
        }
        context.message.guild.controller.kick(user.id)

        if (description == null || description.isEmpty()) return
        if (user.hasPrivateChannel()) user.privateChannel.sendMessage(
                EmbedBuilder().apply {
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setColor(Color.RED)
                    setDescription("$description\n")
                    setFooter("Automatically built message, contact a Moderator for more info", null)
                }.build()
        )
        context.jda.getGuildById(COALESCE_GUILD).publicChannel
                .sendMessage("The user ${user.name} has been kicked for $description!")
    }
}