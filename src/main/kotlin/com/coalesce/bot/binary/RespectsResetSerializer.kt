package com.coalesce.bot.binary

import java.io.File

class RespectsResetSerializer(file: File): BinarySerializer<Long>(file) {
    override fun serializeIn() = inputStream.readLong()
    override fun serializeOut(data: Long) = outputStream.writeLong(data)
}