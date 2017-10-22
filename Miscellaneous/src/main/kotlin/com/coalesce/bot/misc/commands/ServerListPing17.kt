package com.coalesce.bot.misc.commands

import com.coalesce.bot.gson
import com.google.gson.JsonElement
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

/**
 * @author zh32 <zh32 at zh32.de>
</zh32> */
class ServerListPing17 {
    internal val timeout = 7000

    fun readVarInt(`in`: DataInputStream): Int {
        var i = 0
        var j = 0
        while (true) {
            val k = `in`.readByte().toInt()
            i = i or (k and 0x7F shl j++ * 7)
            if (j > 5) throw RuntimeException("VarInt too big")
            if (k and 0x80 != 128) break
        }
        return i
    }

    fun writeVarInt(out: DataOutputStream, numb: Int) {
        var paramInt = numb
        while (true) {
            if (paramInt and 0xFFFFFF80.toInt() == 0) {
                out.writeByte(paramInt)
                return
            }

            out.writeByte(paramInt and 0x7F or 0x80)
            paramInt = paramInt ushr 7
        }
    }

    fun fetchData(address: InetSocketAddress): StatusResponse {
        val socket = Socket()
        socket.soTimeout = this.timeout
        socket.connect(address, timeout)

        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val inputStream = socket.getInputStream()
        val inputStreamReader = InputStreamReader(inputStream)
        val toClose = mutableListOf(dataOutputStream, inputStreamReader)

        val b = ByteArrayOutputStream()
        val handshake = DataOutputStream(b)

        try {
            handshake.writeByte(0x00) // Handshake Packet ID
            writeVarInt(handshake, 4) // Protocol Version
            writeVarInt(handshake, address.hostString.length) // Host String Length
            handshake.writeBytes(address.hostString) // Host String
            handshake.writeShort(address.port) // Port
            writeVarInt(handshake, 1) // State (Handshake = 1)
            writeVarInt(dataOutputStream, b.size()) // Size of handshake packet
            dataOutputStream.write(b.toByteArray()) // Handshake packet

            dataOutputStream.writeByte(0x01) // Size is 1
            dataOutputStream.writeByte(0x00) // Ping Packet ID
            val dataInputStream = DataInputStream(inputStream)
            toClose.add(dataInputStream)
            readVarInt(dataInputStream) // Packet Size
            var id = readVarInt(dataInputStream) // Packet ID
            if (id != 0x00) throw IOException("Invalid Packet ID (Found $id, expected 0)")
            val length = readVarInt(dataInputStream) // Length of JSON String
            if (length == 0) throw IOException("Invalid string length.")

            val `in` = ByteArray(length)
            dataInputStream.readFully(`in`) // Read JSON String
            val json = String(`in`)

            val now = System.currentTimeMillis()
            dataOutputStream.writeByte(0x09) // Packet Size
            dataOutputStream.writeByte(0x01) // Ping Packet ID (0x01)
            dataOutputStream.writeLong(now) // Time

            readVarInt(dataInputStream)
            id = readVarInt(dataInputStream)
            if (id != 0x01) throw IOException("Invalid Packet ID (Found $id, expected 1)")
            dataInputStream.readLong() // Echoed time

            val response = gson.fromJson(json, StatusResponse::class.java)
            response.time = (System.currentTimeMillis() - now).toInt()

            toClose.forEach(Closeable::close)
            return response
        } catch (ex: Exception) {
            toClose.forEach(Closeable::close)
            throw ex
        }
    }

    data class StatusResponse(
        val description: JsonElement,
        val players: Players,
        val version: Version,
        val favicon: String,
        var time: Int
    ) {
        val textDescription: String
            get() = StringBuilder().apply {
                if (description.isJsonObject) {
                    val desc = description.asJsonObject
                    if (desc.has("text")) append(desc["text"])
                    if (desc.has("extra")) desc["extra"].asJsonArray.forEach {
                        val obj = it.asJsonObject
                        if (obj.has("text")) append(obj["text"].asString)
                    }
                } else append(description.asString)
            }.toString()
    }

    data class Players(
        val max: Int,
        val online: Int,
        val sample: List<Player>
    )

    data class Player(
        val name: String,
        val id: String
    )

    data class Version(
        val name: String,
        val protocol: String
    )
}