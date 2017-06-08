package com.coalesce.bot.chatbot

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

    fun clear() {
        //TODO clear all message history
    }
}