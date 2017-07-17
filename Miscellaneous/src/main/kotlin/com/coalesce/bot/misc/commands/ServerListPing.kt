package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.inject.Inject
import java.awt.Color
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import javax.xml.bind.DatatypeConverter
import kotlin.experimental.and

@Command("ServerListPing", "servermotd minecraftservermotd minecraftserverlistping server minecraftserver")
class MinecraftServerListPing @Inject constructor(val executorService: ExecutorService): Embeddables {
    private val serverListPingHandler = ServerListPing17()
    private val errorMessages = mapOf(
            SocketTimeoutException::class.java to "Connection timed out.",
            UnknownHostException::class.java to "Failed to resolve IP address."
    )

    @CommandAlias("Gets the MOTD of a minecraft server.")
    @UserCooldown(30L)
    fun execute(context: CommandContext, serverIP: InetSocketAddress) {
        context(embed().apply {
            embColor = Color.YELLOW
            embTitle = "Connecting to ${serverIP.hostString}..."
        }) {
            executorService.submit {
                try {
                    val info = serverListPingHandler.fetchData(serverIP)
                    editEmbed {
                        embColor = Color(112, 255, 45)
                        setThumbnail(Imgur.upload(info.favicon))
                        embTitle = "Minecraft Server MOTD (${serverIP.hostString})"
                        embDescription = info.description.split(Regex("§[0-9]")).joinToString(separator = "")
                        field("Players", "${info.players.online}/${info.players.max}", true)

                        setFooter("Version: ${info.version.name} (Protocol ${info.version.protocol})", null)
                    }
                } catch (ex: Exception) {
                    editEmbed {
                        embTitle = "Error"
                        embColor = Color(232, 46, 0)

                        embDescription = errorMessages[ex.javaClass as Class<*>] ?:
                            StringBuilder().apply {
                                appendln("Failed to connect to server!")
                                appendln("${ex.javaClass.name}: ${ex.message}")
                                appendln("This has been reported to coalesce developers.")
                            }.toString()
                    }
                    ex.printStackTrace()
                }
            }
        }
    }
}

class ServerListPing17 {
    internal val timeout = 10000

    private fun readVarInt(`in`: DataInputStream): Int {
        var i = 0
        var j = 0
        while (true) {
            val k = `in`.readByte()
            i = i or (k and 0x7F).toInt() shl (j++ * 7)
            if (j > 5) throw RuntimeException("VarInt too big")
            if (k and 128.toByte() != 128.toByte()) break
        }
        return i
    }

    private fun writeVarInt(out: DataOutputStream, int: Int) {
        var n = int
        while (true) {
            if (n and 0xFFFFFF80.toInt() == 0) {
                out.writeByte(n)
                return
            }

            out.writeByte(n and 0x7F or 0x80)
            n = n ushr 7
        }
    }

    fun fetchData(address: InetSocketAddress): StatusResponse {
        val socket = Socket()
        socket.soTimeout = timeout
        socket.connect(address, timeout)

        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val inputStream = socket.getInputStream()
        val inputStreamReader = InputStreamReader(inputStream)
        val toClose = arrayOf(dataOutputStream, inputStreamReader, socket)

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

            readVarInt(dataInputStream) // Size of packet (Not Used)
            val packetId1 = readVarInt(dataInputStream) // Packet ID
            if (packetId1 != 0x00) throw IOException("Invalid Packet ID (Found $packetId1, expected 0)1")

            val length = readVarInt(dataInputStream) // Length of data
            val `in` = ByteArray(length)
            dataInputStream.readFully(`in`)  // Reading data
            val json = String(`in`)


            val now = System.currentTimeMillis()
            dataOutputStream.writeByte(0x09) // Packet size
            dataOutputStream.writeByte(0x01) // Ping Packet ID
            dataOutputStream.writeLong(now) // Time

            readVarInt(dataInputStream)
            val packetId2 = readVarInt(dataInputStream)
            if (packetId2 != 0x01) throw IOException("Invalid Packet ID (Found $packetId2, expected 1)")

            val pingtime = dataInputStream.readLong() // Reading Response
            val response = gson.fromJson(json, StatusResponse::class.java)
            response.time = (now - pingtime).toInt()

            toClose.forEach(Closeable::close)
            return response
        } catch (ex: Exception) {
            toClose.forEach(Closeable::close)
            throw ex
        }
    }


    class StatusResponse(
        val description: String,
        val players: Players,
        val version: Version,
        val favicon: String,
        var time: Int
    )

    class Players(
        val max: Int,
        val online: Int,
        val sample: List<Player>
    )

    class Player(
        val name: String,
        val id: String
    )

    class Version(
        val name: String,
        val protocol: String
    )
}