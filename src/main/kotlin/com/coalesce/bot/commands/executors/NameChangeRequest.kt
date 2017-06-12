package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.*
import com.coalesce.bot.gson
import com.coalesce.bot.punishmentals.PunishmentManager
import com.coalesce.bot.utilities.subList
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.PrivateChannel
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.URL
import java.util.*

class NameChangeRequest @Inject constructor(val bot: Main) {
    private val requestsChannel = bot.jda.getTextChannelById("311317585775951872")

    @RootCommand(
            name = "namechange",
            aliases = arrayOf("namechangerequest", "requestname"),
            globalCooldown = 0.0,
            userCooldown = 720.0,
            type = CommandType.INFORMATION,
            permission = "commands.namechange",
            description = "Request for a name change."
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context("* You must put in a name to request.")
            return
        }

        val name = context.args.joinToString(separator = " ")
        val user = context.author
        requestsChannel.sendMessage(EmbedBuilder()
                .setAuthor(user.name, null, user.avatarUrl?: "https://cdn.discordapp.com/attachments/300377073678286848/313174922677452804/theme_image_22.png")
                .setTitle("Requested name change to " + name, null)
                .setFooter("name ${user.id} $name", null)
                .build()).queue {
            it.addReaction("✅").queue()
            it.addReaction("❎").queue()
            context(context.author, "Name change requested.")
        }
    }

    @JDAListener
    fun onReact(event: MessageReactionAddEvent) {
        if (event.channel == requestsChannel) {
            if (event.user.isBot) return

            val accepted : Boolean
            if (event.reaction.emote.name == "✅") accepted = true
            else if (event.reaction.emote.name == "❎") accepted = false
            else return

            event.channel.getMessageById(event.messageId).queue {
                val footText = it.embeds.first().footer.text.split(" ")
                if (footText.first() != "name") return@queue
                val user = event.guild.getMember(event.jda.getUserById(footText[1]))
                val name = footText.subList(2).joinToString(separator = " ")

                if (accepted) {
                    event.guild.controller.setNickname(user, name).queue {
                        event.channel.sendMessage("Nickname changed to $name.").queue()
                        event.guild.publicChannel.sendMessage("${user.asMention}: Your nickname was changed to $name!").queue()
                    }
                } else {
                    event.channel.sendMessage("The $name nickname was rejected for ${user.effectiveName}.").queue()
                    event.guild.publicChannel.sendMessage("${user.asMention}: Your nickname change to $name was rejected.").queue()
                }
                it.delete().queue()
            }
        }
    }
}
