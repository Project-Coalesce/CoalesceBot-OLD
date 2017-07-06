package com.coalesce.bot.music.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.music.MusicBot
import com.coalesce.bot.music.SKIP_SONG_USERS_PERCENTAGE
import com.coalesce.bot.music.editEmbed
import com.coalesce.bot.utilities.*
import com.google.gson.JsonElement
import com.google.inject.Inject
import java.awt.Color
import java.net.URL
import java.util.concurrent.ExecutorService

@Command("Music", "moosic play")
@UserCooldown(30L)
class Music @Inject constructor(val musicBot: MusicBot, val executorService: ExecutorService): Embeddables {
    private val URL_REGEX = Regex("\\s*(https?|attachment):\\/\\/.+\\..{2,}\\s*")
    private val searchURL = "https://www.googleapis.com/youtube/v3/search?part=id&key=AIzaSyC4R9djLeX1K4qGtP_FI5DzNSiDdE81ICA&" +
            "type=video&fields=items/id/videoId&q="

    @CommandAlias("Add a song or playlist to the track queue")
    fun add(context: CommandContext, @VarArg song: String) {
        if (song.matches(URL_REGEX)) {
            context(embed().apply {
                embColor = Color.YELLOW
                embTitle = "Music Bot"
                embDescription = "Loading song..."
            }) {
                musicBot.loadAndPlay(context.author, context.channel, song, this)
            }
            return
        }

        context(embed().apply {
            embColor = Color.YELLOW
            embTitle = "Music Bot"
            embDescription = "Searching for Youtube video..."
        }) {
            executorService.submit {
                val searchURL = URL(searchURL + song.replace(" ", "+"))
                val items = gson.fromJson(searchURL.openConnection().getInputStream().readText(), JsonElement::class.java).asJsonObject["items"].asJsonArray
                if (items.size() == 0) {
                    editEmbed {
                        embDescription = "No videos found!"
                        embColor = Color(206, 28, 28)
                    }
                    return@submit
                }
                val videoId = items.first().asJsonObject["id"].asJsonObject["videoId"].asString
                val url = "https://youtube.com/watch?v=$videoId"
                editEmbed {
                    embDescription = "Song found! Loading it.."
                }
                musicBot.loadAndPlay(context.author, context.channel, url, this)
            }
        }
    }

    @SubCommand("Stop", "interrupt", "Stops the song and clears the queue")
    fun stop(context: CommandContext) {
        musicBot.stopPlaying(context.guild)
        context("⏹ Cleared the queue and stopped playing.")
    }

    @SubCommand("Skip", "next voteskip", "Skip the current song")
    fun skip(context: CommandContext) {
        val schdl = musicBot[context.guild].scheduler
        if (!schdl.current.isPresent) throw ArgsException("No song is playing!")
        if (schdl.current.get().adder == context.author) {
            context("⏩ Song '**${schdl.current.get().audioTrack.info.title}**' was skipped by song adder.")
            musicBot.skipTrack(context.guild)
            return
        }
        schdl.addSkipVote(context.author, context.channel, musicBot)
        context("Voted to skip current song (${schdl.currentVotes.size}/${context.guild.audioManager.connectedChannel.members.size * (SKIP_SONG_USERS_PERCENTAGE / 100.0)})")
    }
}