package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.editEmbed
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
            embTitle = "Ping 🏓 (Bot's ping not yours!)"
            embDescription = "Checking ping..."
        }) {
            val ping = System.currentTimeMillis() - time
            editEmbed {
                embDescription = null
                embColor = Color.cyan
                field("Response Time", "$ping ms", true)
                field("Discord API", "${context.main.jda.ping} ms", true)
            }
        }
    }
}