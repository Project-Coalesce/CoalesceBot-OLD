package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "Respects", aliases = arrayOf("f", "nahusdream"), description = "Pays respects.", permission = "commands.respects")
class Respects : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        channel.sendMessage("Respects have been paid!").queue { it.delete().queueAfter(1, TimeUnit.MINUTES) }
    }
}