package com.coalesce.bot.music

import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.command.send
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
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
                val manager = GuildMusicManager(playerManager)
                musicManagers.put(guild.idLong, manager)
                manager
            }

            guild.audioManager.sendingHandler = musicManager.sendHandler
            return musicManager
        }
    }

    fun loadAndPlay(user: User, channel: TextChannel, trackUrl: String, message: Message) {
        val musicManager = this[channel.guild]
        val voiceChannel = channel.guild.getMember(user).voiceState.channel ?: throw ArgsException("You must be in a voice channel.")
        val am = channel.guild.audioManager
        if (am.connectedChannel != voiceChannel) {
            if (am.isConnected || am.isAttemptingToConnect) channel.guild.controller.moveVoiceMember(channel.guild.getMember(user), am.connectedChannel).queue()
            else am.openAudioConnection(voiceChannel)
        }

        playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                message.editEmbed {
                    embColor = Color(112, 255, 45)
                    embDescription = "Song added: **${track.info.title}** by **${track.info.author}**"
                }
                musicManager.scheduler.queue(MusicTrack(track, user, message.creationTime))
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val track = playlist.selectedTrack ?: playlist.tracks[0]

                message.editEmbed {
                    embColor = Color(112, 255, 45)
                    embDescription = "Song added: **${track.info.title}** by **${track.info.author}**"
                }
                musicManager.scheduler.queue(MusicTrack(track, user, message.creationTime))
            }

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
        guild.audioManager.closeAudioConnection()
    }

    fun skipTrack(guild: Guild) {
        this[guild].scheduler.nextTrack()
    }
}

fun Message.editEmbed(func: EmbedBuilder.() -> Unit) {
    editMessage(EmbedBuilder(embeds.first()!!).apply(func).build()).queue()
}
