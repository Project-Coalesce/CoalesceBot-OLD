package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import java.util.concurrent.ThreadLocalRandom

@Command("Proximyst", "proxi bae")
class Proximyst {
    @CommandAlias("You will be remembered")
    fun exectue(context: CommandContext) =
            context("Proximyst is a good gal. She helped out a bunch of people, and created this bot." +
                "\nhttp://i.imgur.com/lAkpLgC.png")
}

@Command("Dep", "🅱ep favelalord depressedlolipops28")
class Dep {
    private val images = arrayOf("http://i.imgur.com/PTCUXDQ.png", "http://i.imgur.com/SRG3pYh.png", "http://i.imgur.com/LHP4Ypo.png",
            "http://i.imgur.com/NCWGumn.png", "http://i.imgur.com/sMmzyZP.png", "http://i.imgur.com/UFKYtZZ.png",
            "http://i.imgur.com/sDUPGrO.png")
    @CommandAlias("You will be remembered")
    fun exectute(context: CommandContext) = context("🅱ep contributed more than anyone to this bot," +
            " we are thankful for everything he has done (even if some of the things he made were kind of borked)" +
            "\n${images[ThreadLocalRandom.current().nextInt(images.size)]}")
}