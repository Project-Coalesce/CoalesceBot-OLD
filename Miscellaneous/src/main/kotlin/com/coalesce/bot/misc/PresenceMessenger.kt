package com.coalesce.bot.misc

import com.coalesce.bot.command.JDAEventHandler
import com.coalesce.bot.command.JDAListener
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import java.util.concurrent.ThreadLocalRandom

@JDAEventHandler
class PresenceMessenger {

    private val welcomeMessage = "Welcome, %s, to the Coalesce Discord server!\n" +
            "If you are able to code in an language and would like to have a fancy color for it, use !request <rank>\n" +
            "The currently supported languages include Java, Kotlin, Web, Spigot, Forge, Sponge and Python.\n" +
            "Follow the rules at %s and enjoy your stay!"

    private val pepeEmote = arrayOf("<:feelsbad:335870539950325761>", "<:feelssad:335870539950325761>")

    @JDAListener
    fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        event.guild.publicChannel.sendMessage(String.format(welcomeMessage, event.member.asMention, "<#269178364483338250>"))
    }

    @JDAListener
    fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        event.guild.publicChannel.sendMessage("Today, we see ${event.member.effectiveName} leaving us ${pepeEmote[ThreadLocalRandom.current().nextInt(pepeEmote.size)]}").queue()
    }
}