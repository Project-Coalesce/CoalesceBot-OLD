package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class Pi {
    @RootCommand(
            name = "Pi",
            type = CommandType.INFORMATION,
            permission = "commands.pi",
            description = "Shows the value of Pi.",
            globalCooldown = 5.0
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "${Math.PI}\nhttps://cdn.discordapp.com/attachments/315565346973024256/315680638759993345/enhanced-buzz-29529-1363197741-16.png")
    }
}