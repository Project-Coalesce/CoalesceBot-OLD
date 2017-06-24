package com.coalesce.bot.cocoins.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.GlobalCooldown
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@Command("Boi", "njsblessing")
@GlobalCooldown(20L)
class Boi {
    private val images = arrayOf("http://i.imgur.com/wBjEsAZ.jpg", "http://i.imgur.com/fhHuvIP.jpg", "http://i.imgur.com/k5BqbxH.jpg",
            "http://i.imgur.com/2VeEUTS.jpg", "http://i.imgur.com/hOYMcij.jpg", "http://i.imgur.com/Hx06UHz.jpg", "http://i.imgur.com/DpLS3ZV.jpg",
            "http://i.imgur.com/riXIKEq.jpg", "http://i.imgur.com/f7QugVt.jpg")

    @CommandAlias("Meme command (Boi.)")
    fun execute(context: CommandContext) {
        context("boi. ${images[ThreadLocalRandom.current().nextInt(images.size)]}", deleteAfter = 20L to TimeUnit.SECONDS)
    }
}

@Command("Bork", "depcries")
@GlobalCooldown(20L)
class Bork {
    private val images = arrayOf("http://i.imgur.com/PTCUXDQ.png", "http://i.imgur.com/SRG3pYh.png", "http://i.imgur.com/LHP4Ypo.png",
            "http://i.imgur.com/NCWGumn.png", "http://i.imgur.com/sMmzyZP.png")

    @CommandAlias("Meme command (Bork)")
    fun execute(context: CommandContext) {
        context("Bork ${images[ThreadLocalRandom.current().nextInt(images.size)]}", deleteAfter = 20L to TimeUnit.SECONDS)
    }
}

@Command("EggsDee", "eggsd xd exd")
@GlobalCooldown(20L)
class EggsD {
    @CommandAlias("Meme command (Bork)")
    fun execute(context: CommandContext) {
        context(":egg: :egg: :regional_indicator_d: :regional_indicator_e:")
    }
}

@Command("Hillary", "ctrlalthillary")
@GlobalCooldown(20L)
class CtrlAltHillary {
    @CommandAlias("Meme command (Ctrl + Alt + Hillary)")
    fun execute(context: CommandContext) {
        context("http://i.imgur.com/DYl2RX3.jpg", deleteAfter = 20L to TimeUnit.SECONDS)
    }
}

@Command("Enough", "proxisreaction")
@GlobalCooldown(20L)
class Enough {
    @CommandAlias("Meme command (Enough is enough)")
    fun execute(context: CommandContext) {
        context("Enough is enough.\nhttp://i.imgur.com/KDdaUHW.jpg", deleteAfter = 30L to TimeUnit.SECONDS)
    }
}
