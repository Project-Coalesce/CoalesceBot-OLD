package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class Boi {
    private val images = arrayOf("http://i.imgur.com/wBjEsAZ.jpg", "http://i.imgur.com/fhHuvIP.jpg", "http://i.imgur.com/k5BqbxH.jpg",
            "http://i.imgur.com/2VeEUTS.jpg", "http://i.imgur.com/hOYMcij.jpg", "http://i.imgur.com/Hx06UHz.jpg", "http://i.imgur.com/DpLS3ZV.jpg",
            "http://i.imgur.com/riXIKEq.jpg")

    @RootCommand(
            name = "boi",
            type = CommandType.FUN,
            permission = "commands.boi",
            aliases = arrayOf("njsblessing", "boi"),
            description = "Meme command (**breath in**... boi)",
            globalCooldown = 20.0
    )
    fun execute(context: RootCommandContext) {
        context.send(context.author, "boi. ${images[ThreadLocalRandom.current().nextInt(images.size)]}") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(30, TimeUnit.SECONDS) } }
    }
}