package com.coalesce.commands

import com.coalesce.Bot
import com.coalesce.Constants
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.reflections.Reflections
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode
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

            this[entry.annotation.name.toLowerCase()] = entry

            Arrays.stream(entry.annotation.aliases)
                    .map(String::toLowerCase)
                    .forEach { this[it] = entry }
        } catch (e: Exception) {
            val messageBuilder = MessageBuilder()
            val builder = StringBuilder()
            builder.append(e.toString())
            e.stackTrace.forEach {
                builder.append("\n\tat: $it")
            }
            messageBuilder.appendCodeBlock(builder.toString(), "")
            val message = messageBuilder.build()
            bot.jda.guilds.map { it.publicChannel }.filter { it.canTalk() }.forEach {
                it.sendMessage(message).queue()
            }
        }
    }

    private operator fun set(entry: String, put: CommandEntry) {
        if (entry.contains(" ")) {
            throw IllegalArgumentException("Names cannot include spaces.")
        }
        entries[entry] = put
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
        try {
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
            val cmd = parts[0].toLowerCase()
            val entry = commandMap[cmd] ?: run {
                event.channel.sendMessage(MessageBuilder().append(event.message.author).append(": The command doesn't exist.").build()).queue()
                return
            }
            val args = parts.copyOfRange(1, parts.size) //Arrays.copyOfRange(parts, 1, parts.size)

            try {
                val executor = entry.executor

                val useGlobal: Boolean
                if (executor.annotation.globalCooldown > 0) {
                    val time = (executor.lastUsed + TimeUnit.SECONDS.toMillis(executor.annotation.globalCooldown))
                    if (time > System.currentTimeMillis()) {
                        event.channel.sendMessage(MessageBuilder().append(event.message.author).append(": The command is on a global cooldown for another ")
                                .append(BigDecimal((time - System.currentTimeMillis()) / 1000).setScale(1, RoundingMode.HALF_UP).toDouble()).append(" seconds.").build()).queue()
                        return
                    }
                    useGlobal = true
                } else {
                    useGlobal = false
                }
                if (executor.annotation.userCooldown > 0) {
                    fun check(): Boolean {
                        val author = executor.usages[event.author.idLong] ?: return false
                        val time = (author + TimeUnit.SECONDS.toMillis(executor.annotation.userCooldown))
                        if (time > System.currentTimeMillis()) {
                            event.channel.sendMessage(MessageBuilder().append(event.message.author).append(": The command is on a user cooldown for another ")
                                    .append(BigDecimal((time - System.currentTimeMillis()) / 1000).setScale(1, RoundingMode.HALF_UP).toDouble()).append(" seconds.").build()).queue()
                            return true
                        }
                        return false
                    }
                    if (check()) {
                        return
                    }
                    executor.usages[event.author.idLong] = System.currentTimeMillis()
                }
                if (useGlobal) {
                    executor.lastUsed = System.currentTimeMillis()
                }

                executor.execute(event.channel, event.message, args)
            } catch (ex: Exception) {
                if (ex is CommandError) {
                    event.channel.sendMessage(MessageBuilder().append(event.message.author).appendFormat(": %s", ex.message).build()).queue()
                    return
                }
                println("An error occurred while executing command $cmd")
                ex.printStackTrace()

                try {
                    val embedBuilder = EmbedBuilder()

                    embedBuilder.setColor(Color(232, 46, 0))
                    embedBuilder.setTitle("Error", null)
                    embedBuilder.setDescription("An error occured while trying to handle that command:\n${ex.javaClass.name}: ${ex.message}")

                    event.message.channel.sendMessage(embedBuilder.build()).queue()
                } catch (e: Exception) {
                    e.printStackTrace(); }
            }
        } catch (ex2: Exception) {
            println("An error occurred while executing some command")
            ex2.printStackTrace()

            try {
                val embedBuilder = EmbedBuilder()

                embedBuilder.setColor(Color(232, 46, 0))
                embedBuilder.setTitle("Error", null)
                embedBuilder.setDescription("An error occured while trying to handle that command:\n${ex2.javaClass.name}: ${ex2.message}")

                event.message.editMessage(embedBuilder.build()).queue()
            } catch (e: Exception) {
                e.printStackTrace(); }
        }
    }
}
