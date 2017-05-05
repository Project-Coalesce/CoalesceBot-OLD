package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import java.util.concurrent.TimeUnit

class Rules {
    @RootCommand(
            name = "Rules",
            type = CommandType.INFORMATION,
            permission = "commands.rules",
            description = "Redirects you to the rules.",
            aliases = arrayOf("rulez"),
            globalCooldown = 30.0
    )
    fun execute(context: RootCommandContext) {
        context.send(context.author, "Psst, you... Head over to <#269178364483338250>. They got memes such as this one.")
        context.send("http://i.imgur.com/B50EQKp.png") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(20, TimeUnit.SECONDS) } }
    }
}