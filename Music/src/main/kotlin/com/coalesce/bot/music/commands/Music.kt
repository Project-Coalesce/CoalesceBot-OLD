package com.coalesce.bot.music.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.music.MusicBot
import com.coalesce.bot.music.SKIP_SONG_USERS_PERCENTAGE
import com.coalesce.bot.music.editEmbed
import com.coalesce.bot.utilities.*
import com.google.gson.JsonElement
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import java.awt.Color
import java.net.URL
import java.util.concurrent.ExecutorService

internal val youtubeSearchURL = "https://www.googleapis.com/youtube/v3/search?part=id&key=AIzaSyC4R9djLeX1K4qGtP_FI5DzNSiDdE81ICA&" +
        "type=video&fields=items/id/videoId&q="

@Command("Music", "moosic play")
@UserCooldown(30L)
class Music @Inject constructor(val musicBot: MusicBot, val executorService: ExecutorService): Embeddables {
    private val URL_REGEX = Regex("\\s*(https?|attachment):\\/\\/.+\\..{2,}\\s*")

    @CommandAlias("Add a song or playlist to the track queue")
    fun add(context: CommandContext, source: MusicSource = MusicSource.YOUTUBE, @VarArg song: String) {
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
            embDescription = "Searching for ${source.name.toLowerCase().capitalize()} song..."
        }) {
            executorService.submit {
                val url = source.vidURLProvider(this, song) ?: run {
                    editEmbed {
                        embColor = Color(232, 46, 0)
                        embDescription = "No songs were found."
                    }
                    return@submit
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

    @ReactionListener("SkipSong", arrayOf("skipCheck"))
    fun skipReaction(context: ReactionContext) {
        if (context.author.isBot) return
        skip(context)
    }

    fun skipCheck(event: ReactionContext): Boolean {
        println("Check was called.")
        return !event.author.isBot && event.emote.name == "⏩"
    }

    @SubCommand("Skip", "next voteskip", "Skip the current song")
    fun skip(context: Context) {
        val schdl = musicBot[context.guild].scheduler
        if (!schdl.current.isPresent) throw ArgsException("No song is playing!")
        if (schdl.current.get().adder == context.author) {
            context("⏩ Skipped song '**${schdl.current.get().audioTrack.info.title}**'.")
            musicBot.skipTrack(context.guild)
            return
        }
        if (!schdl.addSkipVote(context.author, context.channel))
            context("Voted to skip current song (${schdl.currentVotes.size}/${(context.guild.audioManager.connectedChannel.members.size * (SKIP_SONG_USERS_PERCENTAGE / 100.0)).toInt()})")
    }

    enum class MusicSource(val vidURLProvider: Message.(String) -> String?): Embeddables {
        YOUTUBE({
            val searchURL = URL(youtubeSearchURL + it.replace(" ", "+"))
            val items = gson.fromJson(searchURL.openConnection().getInputStream().readText(), JsonElement::class.java).asJsonObject["items"].asJsonArray
            if (items.size() <= 0) {
                null
            } else {
                val videoId = items.first().asJsonObject["id"].asJsonObject["videoId"].asString
                editEmbed {
                    embDescription = "Song found! Loading it.."
                }
                "https://youtube.com/watch?v=$videoId"
            }
        }), SOUNDCLOUD({
            throw ArgsException("Soundcloud is not yet supported.")
        }), TWITCH({ "https://twitch.tv/${it.replace(" ", "")}" })
    }

    @SubCommand("Queue", "songqueue currentqueue q", "Shows the current song queue")
    fun queue(context: CommandContext) {
        context(embed().apply {
            val queue = musicBot[context.guild].scheduler.queue
            val limit = context.guild.audioManager.connectedChannel.members.size > 1
            embColor = Color(112, 255, 45)

            if (queue.isEmpty()) {
                embTitle = "The music queue is empty."
            } else {
                embTitle = "Music Bot Queue"
                description {
                    queue.forEachIndexed { index, it ->
                        appendln("#${index + 1}: ${it.toString(limit)} " +
                                "[Added by ${it.adder.asMention} ${it.addTime.formatTimeDiff()} ago]")
                    }
                }
            }
        })
    }

    @JDAListener
    fun eventJoin(event: GuildVoiceLeaveEvent) {
        val schdl = musicBot[event.guild].scheduler
        if (schdl.current.isPresent) {
            schdl.skipVoteCheck(musicBot[event.guild].channel!!)
        }


    }
}