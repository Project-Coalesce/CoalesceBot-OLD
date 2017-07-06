package com.coalesce.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager


class GuildMusicManager(manager: AudioPlayerManager) {
    val player = manager.createPlayer()!!
    val scheduler = TrackScheduler(player)

    init {
        player.addListener(scheduler)
    }

    val sendHandler: MusicBotSendHandler
        get() = MusicBotSendHandler(player)
}