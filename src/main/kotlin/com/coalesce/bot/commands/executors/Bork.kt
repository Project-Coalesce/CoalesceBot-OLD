package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class Bork {

    @RootCommand(
            name = "Bork",
            permission = "commands.bork",
            type = CommandType.FUN,
            description = "Dep likes to bork bot",
            aliases = arrayOf("Dep")
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "Dep likes to bork\nhttp://i.imgur.com/SRG3pYh.png")
    }
}