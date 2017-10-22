package com.coalesce.bot.misc.requests.executors

import com.coalesce.bot.Main
import com.coalesce.bot.command.*
import com.coalesce.bot.misc.requests.Request
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import java.awt.Color

@Command("NewsSuggest", "news newsreport")
class NewsSuggest @Inject constructor(val bot: Main): Request {
    private val reporter = bot.jda.getRoleById(329830467463413762L)
    private val newsChannelID = 329830285044613120L

    @CommandAlias("Send in a news report to have a chance to get it featured on <#329830285044613120L>")
    fun request(context: CommandContext, @VarArg content: String) {
        createRequest(context) {
            embTitle = "News suggestion"
            embDescription = content
            setFooter("news${context.author.idLong}", null)
        }
    }

    @JDAListener
    fun react(event: GuildMessageReactionAddEvent) = uponReact(event, "news")

    override fun response(message: String, embed: MessageEmbed, accepted: Boolean) {
        val user = bot.jda.getUserById(message)
        val content = embed.description

        if (accepted) {
            bot.jda.getTextChannelById(newsChannelID).send(embed().apply {
                embColor = Color.YELLOW
                embTitle = "News"
                embDescription = content
                setFooter("Author: ${user.name}", user.effectiveAvatarUrl)
            }.build())
        }
    }
}