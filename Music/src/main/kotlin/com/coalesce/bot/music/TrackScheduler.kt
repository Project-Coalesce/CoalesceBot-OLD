package com.coalesce.bot.music

import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.command.send
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(private val player: AudioPlayer): AudioEventAdapter() {
    val queue = LinkedBlockingQueue<MusicTrack>()
    var current = Optional.empty<MusicTrack>()
    val currentVotes = mutableListOf<User>()

    fun addSkipVote(user: User, channel: TextChannel, music: MusicBot) {
        if (currentVotes.contains(user)) throw ArgsException("You already voted to skip this song!")
        if (!current.isPresent) throw ArgsException("No song is playing!")
        currentVotes.add(user)
        skipVoteCheck(channel, music)
    }

    fun skipVoteCheck(channel: TextChannel, music: MusicBot) {
        if (!current.isPresent) throw ArgsException("No song is playing!")
        val amountRequired = channel.guild.audioManager.connectedChannel.members.size * (SKIP_SONG_USERS_PERCENTAGE / 100.0)
        if (currentVotes.size >= amountRequired) {
            channel.send("⏩ Song '**${current.get().audioTrack.info.title}**' was skipped by popular vote.")
            music.skipTrack(channel.guild)
        }
    }

    fun queue(track: MusicTrack) {
        if (player.startTrack(track.audioTrack, true)) {
            current = Optional.of(track)
            currentVotes.clear()
        } else queue.offer(track)
    }

    fun nextTrack() {
        val track = queue.poll()
        current = if (track == null) Optional.empty() else Optional.of(track)
        currentVotes.clear()
        player.startTrack(track?.audioTrack, false)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}