package com.coalesce.commands

import com.coalesce.Bot
import com.coalesce.Constants
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.reflections.Reflections
import java.util.*

class CommandMap(private val bot: Bot) {
    internal val entries: MutableMap<String, CommandEntry> = mutableMapOf()

    init {
        val ref = Reflections("com.coalesce.commands.executors")
        ref.getSubTypesOf(CommandExecutor::class.java).forEach { register(it) }
    }

    private fun register(clazz: Class<out CommandExecutor>) {
        try {
            println("Registering command: " + clazz.simpleName)
            val entry = CommandEntry(clazz, this)

            entries[entry.annotation.name.toLowerCase()] = entry

            Arrays.stream<String>(entry.annotation.aliases)
                    .map(String::toLowerCase)
                    .forEach { entries[it] = entry }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    operator fun get(entry: String): CommandEntry? = entries[entry.toLowerCase()]
}

class CommandEntry internal constructor(clazz: Class<out CommandExecutor>, map: CommandMap) {
    val executor: CommandExecutor = clazz.newInstance()
    val annotation: Command

    init {
        executor.jda = Bot.instance.jda
        executor.commandMap = map
        annotation = clazz.getAnnotation(Command::class.java)
        executor.annotation = annotation
    }
}

class CommandListener : ListenerAdapter() {
    val commandMap = CommandMap(Bot.instance)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        var commandLine = event.message.rawContent
        if (!commandLine.startsWith(Constants.COMMAND_PREFIX)) {
            return
        }
        println("Command from ${event.author.name}: $commandLine")
        commandLine = commandLine.substring(Constants.COMMAND_PREFIX.length)
        val parts = commandLine.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        // Alice's famous check which she's not sure how can ever happen
        if (parts.size < 0) {
            return
        }
        if (event.message.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.message.delete().queue()
        }
        val cmd = parts[0].toLowerCase()
        val entry = commandMap[cmd] ?: run {
            event.channel.sendMessage(MessageBuilder().append(event.message.author).append(": The command doesn't exist.").build()).queue()
            return
        }
        val args = Arrays.copyOfRange(parts, 1, parts.size)
        try {
            entry.executor.execute(event.channel, event.message, args)
        } catch (ex: Exception) {
            if (ex is CommandError) {
                event.channel.sendMessage(MessageBuilder().append(event.message.author).appendFormat(": %s", ex.message).build()).queue()
                return
            }
            println("An error occurred while executing command $cmd")
            ex.printStackTrace()
        }
    }
}