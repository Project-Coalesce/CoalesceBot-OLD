package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "hillary", aliases = arrayOf("ctrlalthillary", "ctrlaltdel", "cad", "cah"), description = "Posts a Ctrl Alt Hillary meme into chat.", permission = "commands.hillary",
        globalCooldown = 30, type = CommandType.FUN)
class Hillary : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        channel.sendMessage(MessageBuilder().append(message.author.asMention).append(": https://img.ifcdn.com/images/52044d8cf149969d9c481f9c3cbaff58c888477271180ebff107fd1d1b974a3f_1.jpg").build())
                .queue { it.delete().queueAfter(20, TimeUnit.SECONDS) }
    }
}
