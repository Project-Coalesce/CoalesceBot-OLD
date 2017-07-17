package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import com.google.inject.Inject
import java.awt.Color
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService

@Command("ServerListPing", "servermotd minecraftservermotd minecraftserverlistping server minecraftserver")
class ServerListPing @Inject constructor(val executorService: ExecutorService): Embeddables {
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
                        setThumbnail(Imgur.upload(info.favicon.substring(info.favicon.indexOf(",") + 1), "base64"))
                        embTitle = "Minecraft Server MOTD (${serverIP.hostString})"
                        embDescription = info.textDescription.split(Regex("§[0-9]")).joinToString(separator = "")
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
