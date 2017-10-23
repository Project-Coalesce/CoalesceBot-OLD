package com.coalesce.bot.administration.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.VarArg
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import java.awt.Color

@Command("Broadcast", "bc announce")
class Broadcast @Inject constructor(val jda: JDA): Embeddables {
    @CommandAlias("Broadcast message into all guilds")
    fun broadcast(context: CommandContext, @VarArg message: String) {
        val embed = embed().apply {
            setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
            embTitle = "Broadcast"
            embDescription = message
            embColor = Color(0x5ea81e)
        }.build()
        jda.guilds.forEach {
            val channel = it.getTextChannelsByName("general", true)[0] ?: it.textChannels[0]
            channel.sendMessage(embed).queue()
        }
    }
}