package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "Rules", aliases = arrayOf("Rulez"), permission = "commands.rules", description = "Shows the rules (Well not really)",
        cooldown = 10,type = CommandType.INFORMATION)
class Rules : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        channel.sendMessage(MessageBuilder().append(message.author).appendFormat(": Ptssss, you... Head over to %s\n%s", "<#269178364483338250>",
                "http://i.imgur.com/B50EQKp.png").build()).queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
    }
}
