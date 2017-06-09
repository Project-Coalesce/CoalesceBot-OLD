package com.coalesce.bot.commands.executors

import com.coalesce.bot.chatbot.ChatbotBrain
import com.coalesce.bot.commands.*
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Message

class ChatBot @Inject constructor(val jda: JDA) {

    val brain = ChatbotBrain()

    fun getMessage(message: Message): String {
        val stripped = message.strippedContent.replace(jda.selfUser.asMention, "")
        brain.decay()
        brain.digestSentence(stripped)
        return brain.buildSentence()
    }

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
                    "`!chatbot stats` See general stats\n")
        }
    }

    @SubCommand(
            name = "clear",
            permission = "commands.chatbot.clear"
    )
    fun clear(context: SubCommandContext) {
        brain.clear()
        context(context.author, "Brain cleared")
    }

    @SubCommand(
            name = "stats",
            permission = "commands.chatbot.stats"
    )
    fun stats(context: SubCommandContext) {
        //TODO show some lewd stats
    }
}