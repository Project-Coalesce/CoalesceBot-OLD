package com.coalesce.bot.administration.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import net.dv8tion.jda.core.entities.TextChannel
import java.awt.Color

@Command("ChannelMessage", "sendch sendmsg channelsend")
class ChannelMessage: Embeddables {
    @CommandAlias("Send message into specific channel")
    fun broadcast(context: CommandContext, channel: TextChannel, @VarArg message: String) {
        channel.send(embed().apply {
            setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
            embTitle = "Broadcast"
            embDescription = message
            embColor = Color(0x5ea81e)
        }.build())
    }
}