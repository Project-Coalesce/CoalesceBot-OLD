package com.coalesce.bot.music

import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.command.send
import com.coalesce.bot.utilities.*
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(private val player: AudioPlayer, private val music: MusicBot, private val guild: Guild): AudioEventAdapter(), Embeddables {
    val queue = LinkedBlockingQueue<MusicTrack>()
    private var playing = Optional.empty<MusicTrack>()
    val currentVotes = mutableListOf<User>()
    val pendingTimeouts = mutableListOf<Timeout>()

    val current: Optional<MusicTrack>
        get() = playing

    fun addSkipVote(user: User, channel: TextChannel): Boolean {
        if (currentVotes.contains(user)) throw ArgsException("You already voted to skip this song!")
        if (!guild.audioManager.connectedChannel.members.contains(guild.getMember(user))) throw ArgsException("You must be on the music voice channel!")
        if (!playing.isPresent) throw ArgsException("No song is playing!")
        currentVotes.add(user)
        return skipVoteCheck(channel)
    }

    fun skipVoteCheck(channel: TextChannel): Boolean {
        if (!playing.isPresent) throw ArgsException("No song is playing!")
        val amountRequired = (channel.guild.audioManager.connectedChannel.members.size * (SKIP_SONG_USERS_PERCENTAGE / 100.0)).toInt()
        if (currentVotes.size >= amountRequired) {
            channel.send("⏩ Song '**${playing.get().audioTrack.info.title}**' was skipped by popular vote.")
            music.skipTrack(channel.guild)
            return true
        }
        return false
    }

    fun queue(track: MusicTrack) {
        if (player.startTrack(track.audioTrack, true)) {
            playing = Optional.of(track)
            currentVotes.clear()
        } else {
            queue.offer(track)
        }
    }

    fun nextTrack() {
        pendingTimeouts.forEach(Timeout::stopTimeout)
        pendingTimeouts.clear()

        val track = queue.poll()
        playing = Optional.ofNullable(track)
        currentVotes.clear()
        player.startTrack(track?.audioTrack, false)

        if (track != null) {
            val limit = limit5Min(guild)
            val channel = music[guild].channel!!
            channel.send(embed().apply {
                embTitle = "Now playing"
                embColor = Color(112, 255, 45)
                embDescription = track.toString(limit)
            }.build()) {
                addReaction("⏩").queue()
            }
            if (limit)
                pendingTimeouts.add(timeOutHandler(limitTime.first, limitTime.second) {
                    channel.send("⏩ Skipping song because it has been playing for too long.")
                    nextTrack()
                })
        }
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}