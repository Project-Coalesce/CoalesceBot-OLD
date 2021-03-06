package com.coalesce.bot.binary

import java.io.*

abstract class BinarySerializer<T>(val file: File, val fallback: () -> T) {
    private val baos = ByteArrayOutputStream()
    lateinit var outputStream: DataOutputStream
    lateinit var inputStream: DataInputStream

    fun read(): T {
        if (!file.exists()) return fallback()

        var byteArray: ByteArray = ByteArray(0)
        file.inputStream().use {
            try {
                byteArray = it.readBytes()
            } catch (ex: Exception) {
                System.err.println("Failed to read file.")
                ex.printStackTrace()
                return fallback()
            }
        }

        inputStream = DataInputStream(ByteArrayInputStream(byteArray))

        try {
            return serializeIn()
        } catch (ex: Exception) {
            System.err.println("An error occured while trying to serialize ${file.absolutePath}.")
            throw ex
        }
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