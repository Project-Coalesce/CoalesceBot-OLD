package com.coalesce.bot.command.handlers

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext

@Command("Test", "Just A Test")
class Test {
    @CommandAlias
    fun test(context: CommandContext, message: String) {
        context(message)
    }
}