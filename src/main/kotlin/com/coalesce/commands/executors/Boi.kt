package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@Command(name = "boi", aliases = arrayOf("njsblessing", "boi"), description = "Meme command (**breath in**... boi)", permission = "commands.boi")
class Boi : CommandExecutor() {
    val timeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS)
    var lastUsed: Long = -1

    val images = arrayOf("http://i.imgur.com/wBjEsAZ.jpg", "http://i.imgur.com/fhHuvIP.jpg", "http://i.imgur.com/k5BqbxH.jpg",
            "http://i.imgur.com/2VeEUTS.jpg", "http://i.imgur.com/hOYMcij.jpg", "http://i.imgur.com/Hx06UHz.jpg", "http://i.imgur.com/DpLS3ZV.jpg",
            "http://i.imgur.com/riXIKEq.jpg")

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (lastUsed == -1.toLong() || (System.currentTimeMillis() + timeout) >= lastUsed) {
            lastUsed = System.currentTimeMillis()
            throw CommandError(images[ThreadLocalRandom.current().nextInt(images.size)])
        } else throw CommandError("This command is in cooldown for ${BigDecimal((Math.abs(lastUsed - System.currentTimeMillis())) * 1000).setScale(2, RoundingMode.HALF_EVEN)} seconds.")
    }
}
