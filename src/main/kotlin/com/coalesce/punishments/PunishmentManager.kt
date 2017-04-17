package com.coalesce.punishments

import com.coalesce.Constants
import net.dv8tion.jda.core.entities.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class PunishmentManager {
    val file = File(Constants.DATA_DIRECTORY, "punishment.json")

    fun findPunishments(user: User): JSONArray {
        if(file.exists()){
            val json = JSONObject(file.readText())
            val userData = json.getJSONObject(user.id)

            return userData.getJSONArray("punishments")
        }else return JSONArray()
    }

    fun saveChanges(user: User, array: JSONArray) {
        var json = JSONObject()
        if(file.exists()){
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