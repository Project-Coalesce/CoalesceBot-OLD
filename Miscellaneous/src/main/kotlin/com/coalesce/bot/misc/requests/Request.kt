package com.coalesce.bot.misc.requests

import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.send
import com.coalesce.bot.utilities.Embeddables
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent

interface Request: Embeddables {
    fun createRequest(context: CommandContext, user: User = context.author, embed: EmbedBuilder.() -> Unit) {
        context("Sent to moderators! Good luck :)")
        context.guild.getTextChannelById(311317585775951872L).send(embed().apply(embed), user) {
            addReaction("❌").queue()
            addReaction("✔").queue()
        }
    }

    fun uponReact(event: GuildMessageReactionAddEvent, name: String) {
        if (event.channel.idLong == 311317585775951872L) {
            val response = if (event.reactionEmote.name == "✔") true else if (event.reactionEmote.name == "❌") false else return
            event.channel.getMessageById(event.messageIdLong).queue {
                val embed = it.embeds.first()
                val txt = embed.footer.text
                if (!txt.startsWith(name)) return@queue
                response(txt.substring(name.length), embed, response)
            }
        }
    }

    fun response(message: String, embed: MessageEmbed, accepted: Boolean)
}