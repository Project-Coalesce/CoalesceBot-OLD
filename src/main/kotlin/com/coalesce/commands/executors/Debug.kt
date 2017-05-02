package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import com.coalesce.punishmentals.PunishmentManager
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit

@Command(name = "Debug", permission = "debug", aliases = arrayOf("testing", "test"), description = "A debug command for Proximyst.",
        usage = "<respects: reset>|<quit>", type = CommandType.DEBUG)
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
            if (args.size == 1) {
                syntax()
                return
            }
            if (args[1].equals("reset", true)) {
                Bot.instance.listener.commandMap["respects"]!!.executor.lastUsed = 0
                return
            }
            syntax()
            return
        }
        if (args[0].equals("quit", true)) {
            val data = File(Constants.DATA_DIRECTORY, "data.json")
            if (data.exists()) {
                data.delete()
            }
            PunishmentManager.instance.save()
            Files.write(data.toPath(), Constants.GSON.toJson(mapOf("respectsLastUse" to Bot.instance.listener.commandMap["respects"]!!.executor.lastUsed.toDouble())).toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            Bot.instance.jda.shutdown(true)
            System.exit(0)
            return
        }
        syntax()
    }
}