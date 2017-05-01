package com.coalesce.punishments

import com.coalesce.Bot
import com.coalesce.Constants
import net.dv8tion.jda.core.entities.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PunishmentManager {
    val file = File(Constants.DATA_DIRECTORY, "punishment.json")

    init {
        if(file.exists()){
            val json = JSONObject(file.readText())

            json.toMap().iterator().forEach {
                val user = Bot.instance.jda.getUserById(it.key)
                val obj = JSONObject(it.value)

                if (obj.has("until") && System.currentTimeMillis() < obj.getLong("until")) {
                    val guild = Bot.instance.jda.getGuildById("268187052753944576")
                    val member = guild.getMember(user)
                    val role = guild.getRoleById("303317692608282625")

                    val executorService = Executors.newScheduledThreadPool(1)
                    executorService.schedule(PTimerTask(guild, member, role, executorService), System.currentTimeMillis() - obj.getLong("until"),
                            TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    fun findPunishments(user: User): JSONArray {
        if(file.exists()){
            val json = JSONObject(file.readText())
            val userData = json.getJSONObject(user.id)

            return userData.getJSONArray("punishments")
        }else return JSONArray()
    }

    fun saveChanges(user: User, array: JSONArray) {
        var json = JSONObject()
        if (file.exists()) {
            json = JSONObject(file.readText())
            file.delete()
        }
        file.createNewFile()

        json.put(user.id, array)

        val writer = FileWriter(file)
        writer.write(json.toString())
        writer.close()
    }
}