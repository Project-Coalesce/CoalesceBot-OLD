package com.coalesce.bot.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.entities.User
import java.time.OffsetDateTime

data class MusicTrack(val audioTrack: AudioTrack, val adder: User, val addTime: OffsetDateTime)