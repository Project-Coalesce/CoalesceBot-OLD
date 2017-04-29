package com.coalesce.punishments

import com.coalesce.Bot
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ForcedPunishment(val warning: Boolean, val until: Calendar?, val punisher: User, val by: String, val description: String?) {
    val json = JSONObject()

    init {
        json.put("type", "forced").put("warning", warning).put("by", punisher.id)
        if(description != null) json.put("description", description)
    }

    fun doActUpon(pastPunishments: JSONArray, user: User, channel: MessageChannel): JSONArray {
        createPunishment(user, channel)
        pastPunishments.put(json)

        return pastPunishments
    }

    fun message(user: User, channel: MessageChannel, embedBuilder: EmbedBuilder, untilString: String?) {

        if(warning) embedBuilder.setTitle("⚠ You have received a warning for breaking the rules. ⚠", null)
        else embedBuilder.setTitle("🚫 You have been muted for breaking the rules. 🚫", null)

        val message = StringBuilder()

        if(description != null) message.append("Reason: " + description + "\n")
        if(untilString != null) message.append("Until: $untilString\n")
        message.append("Please refer to <#269178364483338250> before chatting.\n")
        message.append("Repeat offense of the rules will lead to harsher punishments.")

        embedBuilder.setDescription(message.toString())

        var msg = MessageBuilder().setEmbed(embedBuilder.build()).append(user.asMention).build()
        channel.sendMessage(msg).queue()

        val serverLogChannel = Bot.instance.jda.getTextChannelById("299385639437074433")
        val serverLogEmbedBuilder = EmbedBuilder()

        serverLogEmbedBuilder.setAuthor(by, null, punisher.avatarUrl)
        serverLogEmbedBuilder.setColor(Color(255, 64, 64))

        var punishmentShort = "Warning"
        if(!warning)
            if(untilString == null) punishmentShort = "Muted permanently"
            else punishmentShort = "Muted until $untilString"
        serverLogEmbedBuilder.addField("Punishment", punishmentShort, true)
        if(description != null) serverLogEmbedBuilder.addField("Reason", description, true)

        serverLogEmbedBuilder.setTimestamp(msg.creationTime)

        var serverLogMsg = MessageBuilder().setEmbed(serverLogEmbedBuilder.build()).build()
        serverLogChannel.sendMessage(serverLogMsg).queue()

    }

    fun createPunishment(user: User, channel: MessageChannel) {
        val embedBuilder = EmbedBuilder()

        if (warning) {
            message(user, channel, embedBuilder, null)
        } else {
            //Muting
            val guild = Bot.instance.jda.getGuildById("268187052753944576")
            val member = guild.getMember(user)
            val role = guild.getRoleById("303317692608282625")
            guild.controller.addRolesToMember(member, role).queue()

            //Create Mute Message
            var untilString : String? = null
            if(until != null) {
                untilString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(until.time)
                json.put("until", until.timeInMillis)

                val executorService = Executors.newScheduledThreadPool(1)
                executorService.schedule(PTimerTask(guild, member, role, executorService), System.currentTimeMillis() - until.timeInMillis,
                        TimeUnit.MILLISECONDS)
            }

            message(user, channel, embedBuilder, untilString)
        }
    }
}