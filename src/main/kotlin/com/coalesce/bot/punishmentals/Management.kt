package com.coalesce.bot.punishmentals

import com.coalesce.bot.Main
import com.coalesce.bot.dataDirectory
import com.coalesce.bot.gson
import net.dv8tion.jda.core.entities.User
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PunishmentManager internal constructor(bot: Main): Runnable {
    val punishmentsFile = File(dataDirectory, "punishments.json")
    val punishments = mutableMapOf<Long, MutableSet<Punishment>>()
    val tempPunishments = mutableMapOf<Long, MutableSet<Punishment>>()

    init {
        if (punishmentsFile.exists()) {
            val punishmentsJson = mutableMapOf<Long, MutableList<Punishment>>()
            punishmentsFile.reader().use {
                punishmentsJson.putAll(gson.fromJson(it, punishmentsJson::class.java))
            }
            punishmentsJson.forEach { (id, list) ->
                punishments[id] = list.toMutableSet()
                list.forEach {
                    if (!it.expired) {
                        if (System.currentTimeMillis() > it.expiration!!) it.unmute()
                        else tempPunishments[it.expiration!!]?.apply { add(it) } ?: mutableSetOf<Punishment>()
                    }
                }
            }
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 1L, 1L, TimeUnit.MINUTES)
    }

    override fun run() {
        tempPunishments.forEach { k, v ->
            if (System.currentTimeMillis() > k) {
                v.forEach { it.unmute() }
                tempPunishments.remove(k)
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
                } else if (it.expiration == 0L || it.expiration == 1L) {
                    true
                } else {
                    it.expiration!! > System.currentTimeMillis()
                }
            }
        }
        punishments.putAll(copy)
        punishmentsFile.writeText(gson.toJson(punishments))
    }

    operator fun get(id: Long): MutableSet<Punishment> {
        return punishments.computeIfAbsent(id, { mutableSetOf() })
    }

    operator fun get(user: User): MutableSet<Punishment> {
        return this[user.idLong]
    }

    operator fun set(id: Long, punishment: Punishment) {
        punishments[id] = punishments[id]?.apply { add(punishment) } ?: mutableSetOf()
        if (punishment.expiration != null) tempPunishments[punishment.expiration!!]?.apply { add(punishment) } ?: mutableSetOf()
    }

    operator fun set(user: User, punishment: Punishment) {
        this[user.idLong] = punishment
        if (punishment.expiration != null) tempPunishments[punishment.expiration!!]?.apply { add(punishment) } ?: mutableSetOf()
    }
}