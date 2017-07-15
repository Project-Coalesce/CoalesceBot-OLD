package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

@Command("Ping", "pong peeng")
class Ping: Embeddables {

    @CommandAlias("See response time")
    fun execute(context: CommandContext) {
        val time = System.currentTimeMillis()

        context(embed().apply {
            embColor = Color.YELLOW
            embTitle = "Ping 🏓"
            embDescription = "Checking ping..."
        }) {
            val ping = System.currentTimeMillis() - time
            editMessage(EmbedBuilder(embeds.first()).apply {
                embDescription = null
                addField("Response Time", "$ping ms", true)
                addField("Discord API", "${context.main.jda.ping} ms", true)
            }.build()).queue()
        }
    }
}