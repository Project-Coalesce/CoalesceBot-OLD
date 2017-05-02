package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@Command(name = "boi", aliases = arrayOf("njsblessing", "boi"), description = "Meme command (**breath in**... boi)", permission = "commands.boi",
        globalCooldown = 10, type = CommandType.FUN)
class Boi : CommandExecutor() {
    val images = arrayOf("http://i.imgur.com/wBjEsAZ.jpg", "http://i.imgur.com/fhHuvIP.jpg", "http://i.imgur.com/k5BqbxH.jpg",
            "http://i.imgur.com/2VeEUTS.jpg", "http://i.imgur.com/hOYMcij.jpg", "http://i.imgur.com/Hx06UHz.jpg", "http://i.imgur.com/DpLS3ZV.jpg",
            "http://i.imgur.com/riXIKEq.jpg")

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        channel.sendMessage(MessageBuilder().append(message.author.asMention).append(": boi. ")
                .append(images[ThreadLocalRandom.current().nextInt(images.size)]).build()).queue { it.delete().queueAfter(30, TimeUnit.SECONDS) }
    }
}
