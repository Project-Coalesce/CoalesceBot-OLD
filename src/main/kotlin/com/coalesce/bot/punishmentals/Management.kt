package com.coalesce.bot.punishmentals

import com.coalesce.bot.Main
import com.coalesce.bot.dataDirectory
import com.coalesce.bot.gson
import net.dv8tion.jda.core.entities.User
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class PunishmentManager internal constructor(bot: Main) {
    val punishmentsFile = File(dataDirectory, "punishments.json")
    val punishments = mutableMapOf<Long, MutableSet<Punishment>>()
    val mutedRole = bot.jda.getRoleById(303317692608282625)!!

    init {
        if (punishmentsFile.exists()) {
            val punishmentsJson = mutableMapOf<Long, MutableList<Punishment>>()
            punishmentsFile.reader().use {
                punishmentsJson.putAll(gson.fromJson(it, punishmentsJson::class.java))
            }
            punishmentsJson.forEach { (id, list) ->
                punishments[id] = list.toMutableSet()
            }
        }
    }

    fun save() {
        if (punishmentsFile.exists()) {
            punishmentsFile.delete()
        }
        punishmentsFile.createNewFile()
        val copy = mutableMapOf<Long, MutableSet<Punishment>>()
        for ((_, set) in punishments) {
            set.filter {
                if (it.expiration == null) {
                    true
                } else if (it.expiration == 0.toLong() || it.expiration == 1.toLong()) {
                    true
                } else {
                    it.expiration!! > System.currentTimeMillis()
                }
            }
        }
        punishments.putAll(copy)
        Files.write(punishmentsFile.toPath(), gson.toJson(punishments).toByteArray(), StandardOpenOption.WRITE)
    }

    operator fun get(id: Long): MutableSet<Punishment> {
        return punishments.computeIfAbsent(id, { mutableSetOf() })
    }

    operator fun get(user: User): MutableSet<Punishment> {
        return this[user.idLong]
    }

    operator fun set(id: Long, punishment: Punishment) {
        punishments[id] = punishments[id]?.apply { add(punishment) } ?: mutableSetOf()
    }

    operator fun set(user: User, punishment: Punishment) {
        this[user.idLong] = punishment
    }
}