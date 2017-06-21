package com.coalesce.bot.reputation

import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.reputationFile
import net.dv8tion.jda.core.entities.User
import java.io.DataOutputStream
import java.io.File

class RespectsManager {
    private val cache = mutableMapOf<Long, Double>()
    private val serializer: RespectsLeaderboardSerializer

    init {
        val file = reputationFile

        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists())  generateFile(file)

        serializer = RespectsLeaderboardSerializer(file)
    }

    fun readRawData(): MutableMap<Long, Double> = serializer.read()
    fun writeRawData(map: MutableMap<Long, Double>) = serializer.write(map)

    fun generateFile(file: File) {
        file.createNewFile()
        file.outputStream().use {
            DataOutputStream(it).writeLong(-1L)
        }
    }

    operator fun set(user: User, value: Double) {
        val storage = serializer.read()
        storage[user.idLong] = value
        serializer.write(storage)
    }

    operator fun get(from: User): Double {
        return cache[from.idLong] ?: run {
            val userData = serializer.read()[from.idLong]
            if (userData != null) cache[from.idLong] = userData
            userData
        } ?: 0.0
    }

    fun clearCache() = cache.clear()
}
