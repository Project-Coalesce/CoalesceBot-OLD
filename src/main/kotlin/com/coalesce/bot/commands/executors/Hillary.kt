package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import java.util.concurrent.TimeUnit

class Hillary {
    @RootCommand(
            name = "hillary",
            aliases = arrayOf("ctrlalthillary", "ctrlaltdel", "cad", "cah"),
            description = "Posts a Ctrl Alt Hillary meme into chat.",
            permission = "commands.hillary",
            type = CommandType.FUN,
            globalCooldown = 30.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "https://img.ifcdn.com/images/52044d8cf149969d9c481f9c3cbaff58c888477271180ebff107fd1d1b974a3f_1.jpg") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(20, TimeUnit.SECONDS) } }
    }
}