package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class Kys {
    private val images = arrayOf("http://i.imgur.com/wNHRydS.gif", "http://i.imgur.com/4HM0pIm.gifv", "https://media.tenor.co/images/f8c3b7aa341433e9eedca95e8ef9ca64/tenor.gif",
                                    "https://m.popkey.co/50dcd0/EjLDx_s-200x150.gif", "https://i.makeagif.com/media/9-06-2016/aItgFN.gif",
                                    "https://media.tenor.co/images/51302798cf651e8196578b362136ce86/tenor.gif")
    
    //We're not providing any command type, we want it hidden.
    @RootCommand(
            name = "kys",
            permission = "commands.kys",
            aliases = arrayOf("killyourself", "ferdzmiracle"),
            description = "Meme command (KILL YOURSELF, CUNT)",
            globalCooldown = 20.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "<:feelsbad:302954104718884875> ${images[ThreadLocalRandom.current().nextInt(images.size)]}") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(30, TimeUnit.SECONDS) } }
    }
}
