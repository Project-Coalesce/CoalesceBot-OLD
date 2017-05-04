package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import java.util.concurrent.TimeUnit

class Enough {
    @RootCommand(
            name = "enough",
            aliases = arrayOf("proxisreaction"),
            globalCooldown = 30.0,
            type = CommandType.FUN,
            permission = "commands.enough",
            description = "Meme Command (I think you've had enough!)"
    )
    fun execute(context: RootCommandContext) {
        context.send(context.author, "enough is enough. http://i1.kym-cdn.com/photos/images/newsfeed/000/917/654/8a4.jpg") { delete().queueAfter(20, TimeUnit.SECONDS) }
    }
}