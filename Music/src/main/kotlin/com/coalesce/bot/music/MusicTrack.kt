package com.coalesce.bot.music

import com.coalesce.bot.utilities.formatTimeDiff
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.User
import java.util.concurrent.TimeUnit

val limitTime = 5L to TimeUnit.MINUTES
private val fiveMin = limitTime.second.toMillis(limitTime.first)
data class MusicTrack(val audioTrack: AudioTrack, val adder: User, val addTime: Long) {
    fun toString(limit: Boolean) = "**${audioTrack.info.title}** by **${audioTrack.info.author}** " +
            "(${if(limit && audioTrack.duration > fiveMin) "Limited to 5 minutes" else audioTrack.duration.formatTimeDiff()})"
}

fun limit5Min(guild: Guild) = guild.audioManager.connectedChannel.members.count { !it.user.isBot } > 1