package com.coalesce.bot.music

import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.formatTimeDiff
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Color

val SKIP_SONG_USERS_PERCENTAGE = 50.0

class MusicBot: Embeddables {
    private val playerManager = DefaultAudioPlayerManager().apply {
        AudioSourceManagers.registerRemoteSources(this)
        AudioSourceManagers.registerLocalSource(this)
    }
    private val musicManagers = mutableMapOf<Long, GuildMusicManager>()

    operator fun get(guild: Guild): GuildMusicManager {
        synchronized(musicManagers) {
            val musicManager = musicManagers[guild.idLong] ?: run {
                val manager = GuildMusicManager(playerManager, this@MusicBot, guild)
                musicManagers.put(guild.idLong, manager)
                manager
            }

            guild.audioManager.sendingHandler = musicManager.sendHandler
            return musicManager
        }
    }

    fun loadAndPlay(user: User, channel: TextChannel, trackUrl: String, message: Message) {
        val musicManager = this[channel.guild]
        val member = channel.guild.getMember(user)
        val state = member.voiceState
        if (!state.inVoiceChannel()) throw ArgsException("You need to be in a voice channel.")
        val voiceChannel = state.channel
        val am = channel.guild.audioManager
        synchronized(am) {
            if (am.isConnected && am.connectedChannel != voiceChannel) channel.guild.controller.moveVoiceMember(member, am.connectedChannel).queue()
            else if(!am.isConnected && !am.isAttemptingToConnect) am.openAudioConnection(voiceChannel)
        }
        musicManager.channel = channel

        playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                val mtrack = MusicTrack(track, user, System.currentTimeMillis())
                message.editEmbed {
                    embColor = Color(112, 255, 45)
                    embDescription = "Song added: ${mtrack.toString(limit5Min(channel.guild))}"
                }
                musicManager.scheduler.queue(mtrack)
                if (musicManager.scheduler.current.get() == mtrack)
                    message.addReaction("⏩").queue()
            }

            override fun playlistLoaded(playlist: AudioPlaylist) = trackLoaded(playlist.selectedTrack ?: playlist.tracks[0])

            override fun noMatches() {
                message.editEmbed {
                    embColor = Color(232, 46, 0)
                    embDescription = "Song not found!"
                }
            }

            override fun loadFailed(exception: FriendlyException) {
                message.editEmbed {
                    embColor = Color(232, 46, 0)
                    embDescription = "That song could not be played:\n${exception.message}"
                }
            }
        })
    }

    fun stopPlaying(guild: Guild) {
        this[guild].scheduler.queue.clear()
        this[guild].scheduler.nextTrack()
    }

    fun skipTrack(guild: Guild) {
        this[guild].scheduler.nextTrack()
    }
}

fun Message.editEmbed(func: EmbedBuilder.() -> Unit) {
    editMessage(EmbedBuilder(embeds.first()!!).apply(func).build()).queue()
}
