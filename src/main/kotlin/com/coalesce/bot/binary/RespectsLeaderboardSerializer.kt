package com.coalesce.bot.binary

import java.io.File

class RespectsLeaderboardSerializer(file: File): BinarySerializer<MutableMap<Long, Double>>(file, { mutableMapOf() }) {
    override fun serializeIn(): MutableMap<Long, Double> {
        val map = mutableMapOf<Long, Double>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break

            val value = inputStream.readDouble()
            map[long] = value
        }

        return map
    }

    override fun serializeOut(data: MutableMap<Long, Double>) {
        data.forEach { k, v ->
            outputStream.writeLong(k)
            outputStream.writeDouble(v)
        }
        outputStream.writeLong(-1L)
    }

}