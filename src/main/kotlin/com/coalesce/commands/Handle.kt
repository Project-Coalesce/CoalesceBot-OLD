package com.coalesce.commands

import com.coalesce.Bot
import com.coalesce.Constants
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.reflections.Reflections
import java.util.*
import java.util.concurrent.TimeUnit

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
    val cooldownBlock = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS)
    var lastBlock = System.currentTimeMillis() - cooldownBlock

    override fun onMessageReceived(event: MessageReceivedEvent) {
        var commandLine = event.message.rawContent
        if (!commandLine.startsWith(Constants.COMMAND_PREFIX)) {
            return
        }
        println("Command from ${event.author.name}: $commandLine")
        commandLine = commandLine.substring(Constants.COMMAND_PREFIX.length)
        val parts = commandLine.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        // Alice's (now proxi enhanced) famous check which she's not sure how can ever happen
        if (parts.isEmpty()) {
            return
        }
        if (event.message.guild.selfMember.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.message.delete().queue()
        }
        if (event.channel == event.guild.publicChannel && event.guild.idLong == 268187052753944576L) {
            if (event.guild.getTextChannelById(301114815596855297L) != null) {
                if (System.currentTimeMillis() >= (lastBlock + cooldownBlock)) {
                    event.channel.sendMessage(MessageBuilder().append(event.message.author).appendFormat(": Use the channel %s\n%s", event.guild.getTextChannelById(301114815596855297L).asMention,
                            "https://cdn.discordapp.com/attachments/268187052753944576/305747595215765504/18034176_312913882456314_8270435912214209153_n.png").build()).queue {
                        it.delete().queueAfter(10, TimeUnit.SECONDS)
                    }
                    lastBlock = System.currentTimeMillis()
                }
            }
            return
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