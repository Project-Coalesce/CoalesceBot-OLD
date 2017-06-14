package com.coalesce.bot.binary

import java.io.File

class RespectsLeaderboardSerializer(file: File): BinarySerializer<MutableMap<String, Double>>(file) {
    override fun serializeIn(): MutableMap<String, Double> {
        val map = mutableMapOf<String, Double>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break

            val value = inputStream.readDouble()
            map[long.toString()] = value
        }

        return map
    }

    override fun serializeOut(data: MutableMap<String, Double>) {
        data.forEach { k, v ->
            outputStream.writeLong(k.toLong())
            outputStream.writeDouble(v)
        }
        outputStream.writeLong(-1L)
    }

}