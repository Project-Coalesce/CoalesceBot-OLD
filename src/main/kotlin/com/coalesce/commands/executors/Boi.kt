package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.ThreadLocalRandom

@Command(name = "boi", aliases = arrayOf("njsblessing"), description = "**breath in**... boi", permission = "commands.boi")
class Boi : CommandExecutor() {

    val images = arrayOf("http://i.imgur.com/wBjEsAZ.jpg", "http://i.imgur.com/fhHuvIP.jpg", "http://i.imgur.com/k5BqbxH.jpg",
            "http://i.imgur.com/2VeEUTS.jpg", "http://i.imgur.com/hOYMcij.jpg", "http://i.imgur.com/Hx06UHz.jpg", "http://i.imgur.com/DpLS3ZV.jpg",
            "http://i.imgur.com/riXIKEq.jpg")

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        throw CommandError(images[ThreadLocalRandom.current().nextInt(images.size)])
    }
}
