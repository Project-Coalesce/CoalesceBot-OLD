package com.coalesce.bot.binary

import java.io.File

class RespectsLeaderboardSerializer(file: File): BinarySerializer<MutableMap<String, Any?>>(file) {
    override fun serializeIn(): MutableMap<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break

            val value = inputStream.readDouble()
            map[long.toString()] = value
        }

        return map
    }

    override fun serializeOut(data: MutableMap<String, Any?>) {
        data.forEach { k, v ->
            outputStream.writeLong(k.toLong())
            outputStream.writeDouble(v as Double)
        }
        outputStream.writeLong(-1L)
    }

}