package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "Debug", permission = "debug", aliases = arrayOf("testing", "test"), description = "A debug command for Proximyst.",
        usage = "<respects: reset>", type = CommandType.DEBUG)
class Debug : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (message.author.idLong != 181470050039889920L) {
            return // Just let it delete the message.
        }
        fun syntax() {
            channel.sendMessage("Syntax: ${annotation.usage}").queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
        }
        if (args.isEmpty()) {
            syntax()
            return
        }
        if (args[0].equals("respects", true)) {
            if (args[1].equals("reset", true)) {
                Bot.instance.respectsLastUse = -1f
                return
            }
            syntax()
            return
        }
        syntax()
    }
}