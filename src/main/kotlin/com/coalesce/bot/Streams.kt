package com.coalesce.bot

import com.coalesce.bot.utilities.quietly
import net.dv8tion.jda.core.entities.MessageChannel
import java.io.OutputStream

class ChatOutputStream(val channel: MessageChannel): OutputStream() {
    private var lineBuilder = StringBuilder()

    override fun write(i: Int) {
        val char = i.toChar()
        val stringInstance = Character.toString(char)

        lineBuilder.append(stringInstance)

        if (stringInstance == "\n") {
            quietly { channel.sendMessage(lineBuilder.toString()).queue() }
            lineBuilder = StringBuilder()
        }
    }
}