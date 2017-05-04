package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext

class UrbanDefinition {
    @RootCommand(
            name = "Definition",
            aliases = arrayOf("define", "dictionary", "urban"), description = "Defines a word or phrase with Urban Dictionary.",
            permission = "command.definition",
            globalCooldown = 5.0,
            type = CommandType.INFORMATION
    )
    fun execute(context: RootCommandContext) {
        context.send("Unimplemented.")
    }
}