package com.coalesce.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel

class GuildMusicManager(manager: AudioPlayerManager, musicBot: MusicBot, guild: Guild) {
    val player = manager.createPlayer()!!
    val scheduler = TrackScheduler(player, musicBot, guild)
    var channel: TextChannel? = null

    init {
        player.addListener(scheduler)
    }

    val sendHandler: MusicBotSendHandler
        get() = MusicBotSendHandler(player)
}