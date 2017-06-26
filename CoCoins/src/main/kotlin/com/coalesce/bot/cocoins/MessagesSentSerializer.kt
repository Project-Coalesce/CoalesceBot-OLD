package com.coalesce.bot.cocoins

import com.coalesce.bot.binary.BinarySerializer
import java.io.File

class MessagesSentSerializer(file: File): BinarySerializer<MutableMap<Long, Int>>(file, { mutableMapOf() }) {
    override fun serializeOut(data: MutableMap<Long, Int>) {
        data.forEach { k, v ->
            outputStream.writeLong(k)
            outputStream.writeInt(v)
        }
        outputStream.writeLong(-1L)
    }

    override fun serializeIn(): MutableMap<Long, Int> {
        val map = mutableMapOf<Long, Int>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break
            map[long] = inputStream.readInt()
        }

        return map
    }
}