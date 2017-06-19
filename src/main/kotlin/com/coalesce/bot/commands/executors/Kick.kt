package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.ArgsException
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.PrivateChannel
import java.awt.Color

class Kick @Inject constructor(val bot: Main) {
    @RootCommand(
            name = "Kick",
            permission = "commands.kick",
            type = CommandType.ADMINISTRATION,
            description = "Kicks the given user out of the server."
    )
    fun execute(context: RootCommandContext) {
        val user = context.mentioned
        var description: String? = null
        if (context.args.size > 1) {
            description = context.args.copyOfRange(1, context.args.size).joinToString(separator = " ")
        }
        context.message.guild.controller.kick(user.id)

        if (description == null || description.isEmpty()) return

        val privateChannel: PrivateChannel
        if (!user.hasPrivateChannel()) privateChannel = user.openPrivateChannel().complete()
        else privateChannel = user.privateChannel

        privateChannel.sendMessage(
                EmbedBuilder().apply {
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setColor(Color.RED)
                    setDescription("You were kicked from ${context.message.guild.name}.\n$description")
                    setFooter("Automatically built message, contact a Moderator for more info", null)
                }.build()
        ).queue()

        context.message.guild.publicChannel
                .sendMessage("The user ${user.name} has been kicked for \"$description\"!").queue()
    }
}