package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.timeOutHandler
import java.util.*
import java.util.concurrent.TimeUnit

@Command("RemindMe", "remind")
class RemindMe {
    @CommandAlias("Reminds you after a while")
    fun execute(context: CommandContext, time: Calendar, @VarArg message: String) {
        if (System.currentTimeMillis() <= time.timeInMillis) throw ArgsException("Invalid time.")
        context("I'll be reminding you!")
        timeOutHandler(System.currentTimeMillis() - time.timeInMillis, TimeUnit.MILLISECONDS) {
            context.usePCh { send("Reminder: $message") }
        }
    }
}