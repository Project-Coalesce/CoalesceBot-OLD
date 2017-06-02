package com.coalesce.bot.punishmentals

import com.coalesce.bot.Main
import com.google.gson.*
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

data class Punishment(val bot: Main, val reason: Reason, val punished: User, val punishee: User, val description: String?,
                      var expiration: Long?, val guild: Guild, var expired: Boolean = false) {
    fun unmute() {
        val member = guild.getMember(punished)
        val role = guild.getRoleById("303317692608282625")
        guild.controller.addRolesToMember(member, role).queue()

        expired = true
    }

    fun doActUpon(record: Collection<Punishment>, channel: MessageChannel) {
        val amount = record.size + 1 // Past record + this
        val totalSev = run {
            var ret = 0
            for (it in record) {
                ret += it.reason.severity
                if (ret >= 7) {
                    ret = 7
                    break
                }
            }
            ret
        }
        val severity = Math.min(Math.max(totalSev, 1), PunishmentTimes.values().size)

        fun message(channel: MessageChannel, embed: EmbedBuilder, warning: Boolean, until: String?) {
            if (warning) {
                embed.setTitle("⚠ You have received a warning for breaking the rules. ⚠", null)
            } else {
                embed.setTitle("🚫 You have been muted for breaking the rules. 🚫", null)
            }
            val message = StringBuilder()

            message.append("Reason: ").append(reason.description).append("\n")
            if (description != null) {
                message.append("Description: ").append(description).append("\n")
            }
            if (until != null) {
                message.append("Expires: ").append(until).append("\n")
            }
            message.append("\nPlease refer to <#269178364483338250> before chatting again.\n") // TODO: Make channel configurable per guild.
            message.append("Repeated offences will lead to a harsher punishment.\n")
            message.append("You currently have $amount offences on your record.")
            embed.setDescription(message.toString())

            channel.sendMessage(MessageBuilder().append(punished.asMention).setEmbed(embed.build()).build()).queue()

            val logChannel = bot.jda.getTextChannelById(299385639437074433) // TODO: Make channel configurable per guild.
            val logEmbed = EmbedBuilder()
            logEmbed.setAuthor(punishee.name, null, punishee.avatarUrl)
            logEmbed.setColor(Color(255, 64, 64))

            val short: String
            if (!warning) {
                if (until == null) {
                    short = "Muted permanently"
                } else {
                    short = "Muted until $until"
                }
            } else {
                short = "Warning"
            }
            logEmbed.addField("Punishment", short, true)
            logEmbed.addField("Reason", reason.description, true)
            try {
                logChannel.sendMessage(logEmbed.build()).queue()
            } catch (_: Exception) {
                // Not caught due to permissions may be removed from logging channel at any time.
            }
        }

        val time = PunishmentTimes.values()[severity]
        expiration = time.time.toLong()
        if (time.timeUnit == -1 || !time.permanent) {
            message(channel, EmbedBuilder(), true, null)
        } else {
            //Muting
            val member = guild.getMember(punished)
            val role = guild.getRoleById("303317692608282625")
            guild.controller.addRolesToMember(member, role).queue()

            //Create Mute Message
            var untilString: String? = "An error occurred "
            if (time.timeUnit > 0) {
                val calendar = Calendar.getInstance()
                calendar.add(time.timeUnit, time.time)
                untilString = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(calendar.time)
            }

            message(channel, EmbedBuilder(), false, untilString)
        }
    }
}

class PunishmentSerializer(val bot: Main) : JsonDeserializer<Punishment>, JsonSerializer<Punishment> {
    override fun serialize(src: Punishment, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val obj = JsonObject()
        obj.add("reason", JsonPrimitive(src.reason.name.toUpperCase()))
        obj.add("punished", JsonPrimitive(src.punished.id))
        obj.add("punishee", JsonPrimitive(src.punishee.id))
        obj.add("guild", JsonPrimitive(src.guild.id))
        obj.add("description", if (src.description == null) JsonNull.INSTANCE else JsonPrimitive(src.description))
        obj.add("expiration", if (src.expiration == null) JsonNull.INSTANCE else JsonPrimitive(src.expiration))
        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Punishment? {
        val obj = json.asJsonObject
        val reason = Reason.valueOf(obj.get("reason").asString.toUpperCase())
        val punished = bot.jda.getUserById(obj.get("punished").asString)
        val punishee = bot.jda.getUserById(obj.get("punishee").asString)
        val guild = bot.jda.getGuildById(obj.get("guild").asString)
        val description = obj.get("reason")?.asString // String?
        val expiration = obj.get("expiration")?.asLong
        return Punishment(bot, reason, punished, punishee, description, expiration, guild)
    }
}