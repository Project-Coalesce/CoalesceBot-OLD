package com.coalesce.bot.commands.executors

import com.coalesce.bot.chatbot
import com.coalesce.bot.commands.*

class ChatBot {

    @RootCommand(
            name = "ChatBot",
            permission = "commands.chatbot",
            type = CommandType.DEBUG,
            description = "ChatCommand brain functions"
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, "\n**Usage:**\n" +
                    "`!chatbot clear` Clear the brain\n" +
                    "`!chatbot stats` See general stats\n" +
                    "`!chatbot disable` Uh... If everything goes wrong we have a panic button")
        }
    }

    @SubCommand(
            name = "clear",
            permission = "commands.chatbot.clear"
    )
    fun clear(context: SubCommandContext) {
        chatbot.clear()
        context(context.author, "Brain cleared")
    }

    @SubCommand(
            name = "stats",
            permission = "commands.chatbot.stats"
    )
    fun stats(context: SubCommandContext) {
        //TODO show some lewd stats
    }

    @SubCommand(
            name = "disable",
            permission = "commands.chatbot.disable"
    )
    fun disable(context: SubCommandContext) {
        val value = chatbot.isDisabled
        chatbot.isDisabled = !value
        context("** Panic button has been set to: ${value.toString().capitalize()}")
    }
}