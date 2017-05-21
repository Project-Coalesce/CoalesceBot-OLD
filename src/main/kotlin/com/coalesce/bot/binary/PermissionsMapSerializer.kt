package com.coalesce.bot.binary

import java.io.File

class PermissionsMapSerializer(file: File): BinarySerializer<MutableMap<String, Boolean>>(file) {
    override fun serializeIn(): MutableMap<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break

            map[long.toString()] = inputStream.readBoolean()
        }

        return map
    }

    override fun serializeOut(data: MutableMap<String, Boolean>) {
        data.forEach { k, v ->
            outputStream.writeLong(k.toLong())
            outputStream.writeBoolean(v)
        }
        outputStream.writeLong(-1L)
    }

}