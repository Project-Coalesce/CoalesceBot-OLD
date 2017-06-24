package com.coalesce.bot.binary

import java.io.File

class LongSerializer(file: File, default: Long = -1): BinarySerializer<Long>(file, { default }) {
    override fun serializeIn() = inputStream.readLong()
    override fun serializeOut(data: Long) = outputStream.writeLong(data)
}