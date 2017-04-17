package com.coalesce.punishments

import com.coalesce.Bot
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Channel
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

class Punishment(val reason: Reason, val by: String, val description: String?) {
    val json = JSONObject()

    init {
        json.put("reason", reason.toString()).put("by", by)
        if(description != null) json.put("description", description)
    }

    fun doActUpon(pastPunishments: JSONArray, user: User, channel: MessageChannel): JSONArray {
        var amount = 0
        var severity = 0

        pastPunishments.iterator().forEach {
            if(it is JSONObject){
                amount ++

                var reason = Reason.valueOf(it.getString("reason"))
                severity += reason.severity
            }
        }

        severity = Math.min(Math.max(severity, 1), PunishmentTimes.values().size) //Severity is in range: 1-7
        /*
            1: Warn
            2: Second Warning
            3: Mute for 1 hour
            4: Mute for 8 hours
            5: Mute for 2 days
            6: Mute for a week
            7: Permanently Mute
        */

        createPunishment(severity, amount, user, channel)
        pastPunishments.put(json)

        return pastPunishments
    }

    fun createPunishment(severity: Int, amount: Int, user: User, channel: MessageChannel){
        val embedBuilder = EmbedBuilder()
        val punishmentTime = PunishmentTimes.values()[severity]

        if(punishmentTime.timeUnit < 0){
            //Create Warning
            embedBuilder.setTitle("⚠ You have received a warning for breaking the rules. ⚠", null)

            val message = StringBuilder()

            message.append("Reason: $reason.description\n")
            if(description != null) message.append("Description: $description\n")
            message.append("Please refer to <#269178364483338250> before chatting.\n")
            message.append("Repeat offense of the rules will lead to harsher punishments.")

            embedBuilder.setColor(Color(255, 221, 0))
            embedBuilder.setDescription(message.toString())

            var msg = MessageBuilder().setEmbed(embedBuilder.build()).append(user.asMention).build()
            channel.sendMessage(msg).queue()
        }else{
            //Muting
            val guild = Bot.instance.jda.getGuildById("268187052753944576") //Idk how to get it from the channel don't judge me
            val member = guild.getMember(user)
            val role = guild.getRoleById("303317692608282625")
            guild.controller.addRolesToMember(member, role)

            //TODO Make the mute temporary

            //Create Mute Message
            embedBuilder.setTitle("🚫 You have been muted for breaking the rules. 🚫", null)

            val calendar = Calendar.getInstance()
            calendar.add(punishmentTime.timeUnit, punishmentTime.time)
            val untilString = SimpleDateFormat("dd/MM/yyyy hh:mm").format(calendar.time)

            val message = StringBuilder()

            message.append("Reason: $reason.description\n")
            message.append("Until: $untilString\n")
            if(description != null) message.append("Description: $description\n")
            message.append("Please refer to <#269178364483338250> before chatting.\n")
            message.append("Repeat offense of the rules will lead to harsher punishments.")

            embedBuilder.setColor(Color(255, 221, 0))
            embedBuilder.setDescription(message.toString())

            var msg = MessageBuilder().setEmbed(embedBuilder.build()).append(user.asMention).build()
            channel.sendMessage(msg).queue()
        }
    }
}