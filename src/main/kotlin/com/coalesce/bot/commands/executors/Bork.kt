package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import java.util.concurrent.TimeUnit

class Bork {
    @RootCommand(
            name = "Bork",
            permission = "commands.bork",
            type = CommandType.FUN,
            description = "Dep likes to bork bot",
            aliases = arrayOf("depcries"),
            globalCooldown = 20.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "Dep likes to bork\nhttp://i.imgur.com/SRG3pYh.png") {
            ifwithDo(canDelete, context.message.guild) { delete().queueAfter(30, TimeUnit.SECONDS) }
        }
    }
}