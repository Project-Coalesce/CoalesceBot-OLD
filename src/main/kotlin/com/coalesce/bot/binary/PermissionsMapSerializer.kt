package com.coalesce.bot.binary

import java.io.File

class PermissionsMapSerializer(file: File): BinarySerializer<MutableMap<String, Boolean>>(file) {
    override fun serializeIn(): MutableMap<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()

        while (true) {
            val string = inputStream.readUTF()
            if (string == ";") break

            map[string] = inputStream.readBoolean()
        }

        return map
    }

    override fun serializeOut(data: MutableMap<String, Boolean>) {
        data.forEach { k, v ->
            outputStream.writeUTF(k)
            outputStream.writeBoolean(v)
        }
        outputStream.writeUTF(";")
    }

}