package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.ExecutorService
import javax.imageio.ImageIO
import javax.xml.bind.DatatypeConverter
import kotlin.experimental.and

@Command("DickSize", "dicc dick penis penissize")
class DickSize: Embeddables {
    @CommandAlias("Take a look at someone's (Or your own) dick")
    fun execute(context: CommandContext, user: User = context.author) {
        val dickPart = StringBuilder().apply { (0..Random(user.idLong).nextInt(30)).forEach { append("=") } }.toString()
        context("${user.asMention}'s dick: 8${dickPart}D")
    }
}
