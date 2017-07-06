package com.coalesce.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.BlockingQueue
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.core.audio.AudioSendHandler


class MusicBotSendHandler(private val audioPlayer: AudioPlayer): AudioSendHandler {
    private var lastFrame: AudioFrame? = null

    override fun canProvide(): Boolean {
        if (lastFrame == null) lastFrame = audioPlayer.provide()
        return lastFrame != null
    }

    override fun provide20MsAudio(): ByteArray? {
        if (lastFrame == null) lastFrame = audioPlayer.provide()
        val data = lastFrame?.data
        lastFrame = null
        return data
    }

    override fun isOpus(): Boolean = true
}