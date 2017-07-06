package com.coalesce.bot.administration.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import java.awt.Color

@Command("Broadcast", "bc announce")
class Broadcast: Embeddables {
    @CommandAlias("Broadcast message into all guilds")
    fun broadcast(context: CommandContext, @VarArg message: String) {
        val embed = embed().apply {
            setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
            embTitle = "Broadcast"
            embDescription = message
            embColor = Color(0x5ea81e)
        }.build()

        context.main.jda.guilds.forEach { it.publicChannel.send(embed) }
    }
}