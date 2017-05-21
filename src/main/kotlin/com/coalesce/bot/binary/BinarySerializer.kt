package com.coalesce.bot.binary

import java.io.*

abstract class BinarySerializer<T>(val file: File) {
    private val baos = ByteArrayOutputStream()
    lateinit var outputStream: DataOutputStream
    lateinit var inputStream: DataInputStream

    fun read(): T {
        var byteArray: ByteArray = ByteArray(0)
        file.inputStream().use {
            byteArray = it.readBytes()
        }

        inputStream = DataInputStream(ByteArrayInputStream(byteArray))
        return serializeIn()
    }

    fun write(data: T) {
        outputStream = DataOutputStream(baos)
        serializeOut(data)

        val bytes = baos.toByteArray()
        baos.reset()

        if (file.exists()) file.delete()
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        file.createNewFile()

        file.outputStream().use {
            it.write(bytes)
        }
    }

    abstract fun serializeOut(data: T)
    abstract fun serializeIn(): T
}