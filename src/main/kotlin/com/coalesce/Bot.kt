package com.coalesce

import com.coalesce.Constants.DATA_DIRECTORY
import com.coalesce.commands.CommandListener
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Bot {
    lateinit var jda: JDA
    val executor: ExecutorService = Executors.newFixedThreadPool(6)
    var respectsLastUse: Float = -1f
    init {
        instance = this
    }

    fun run(token: String) {
        if (!DATA_DIRECTORY.exists()) {
            if (DATA_DIRECTORY.mkdirs()) {
                println("The data directory didn't exist already and was created.")
            }
        }
        jda = JDABuilder(AccountType.BOT).setAudioEnabled(false).setCorePoolSize(4).setToken(token).buildBlocking()
        jda.guilds.map { it.publicChannel }.filter { it.canTalk() }.forEach { it.sendMessage("The bot is now enabled and ready for user input.").queue { it.delete().queueAfter(5, TimeUnit.SECONDS) } }

        jda.presence.game = Game.of("under the blanket...")

        jda.addEventListener(CommandListener())
    }

    companion object {
        lateinit var instance: Bot
    }
}