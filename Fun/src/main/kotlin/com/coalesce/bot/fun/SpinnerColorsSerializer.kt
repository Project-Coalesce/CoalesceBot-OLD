package com.coalesce.bot.`fun`

import com.coalesce.bot.binary.BinarySerializer
import com.coalesce.bot.dataDirectory
import java.awt.Color
import java.io.File

class SpinnerColorsSerializer: BinarySerializer<MutableMap<Long, Color>>(File(dataDirectory, "spinnerColors.dat"), { mutableMapOf() }) {
    override fun serializeIn(): MutableMap<Long, Color> {
        val map = mutableMapOf<Long, Color>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break

            map[long] = Color(inputStream.readByte().toInt(), inputStream.readByte().toInt(), inputStream.readByte().toInt(), inputStream.readByte().toInt())
        }

        return map
    }

    override fun serializeOut(data: MutableMap<Long, Color>) {
        data.forEach { k, v ->
            outputStream.writeLong(k)

            outputStream.writeByte(v.red)
            outputStream.writeByte(v.green)
            outputStream.writeByte(v.blue)
            outputStream.writeByte(v.alpha)
        }
        outputStream.writeLong(-1L)
    }
}