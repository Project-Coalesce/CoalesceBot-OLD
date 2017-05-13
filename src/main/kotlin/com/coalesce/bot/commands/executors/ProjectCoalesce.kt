package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class ProjectCoalesce {
    @RootCommand(
            name = "ProjectCoalesce",
            globalCooldown = 30.0,
            description = "Shows a brief description of what is project coalesce for those who are wondering.",
            type = CommandType.INFORMATION,
            aliases = arrayOf("whatisprojectcoalesce", "coalesce", "whatiscoalesce", "whatispcoalesce", "whatisprojcoalesce"),
            permission = "commands.coalesce"
    )
    fun execute(context: RootCommandContext) {
        context(context.author, "Project-Coalesce is a diverse group of developers. We specialize in plugins using the Spigot-API, however we are open to all kinds of development.")
    }
}