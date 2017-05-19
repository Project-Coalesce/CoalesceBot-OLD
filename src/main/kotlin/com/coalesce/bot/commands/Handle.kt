package com.coalesce.bot.commands

import com.coalesce.bot.Main
import com.coalesce.bot.commandPrefix
import com.coalesce.bot.commandPrefixLen
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.awt.Color
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class Listener internal constructor() : ListenerAdapter(), Embeddables {
    val registry = CommandRegistry()
    val checks = mutableSetOf<Predicate<CommandContext>>()
    private val cooldowns = mutableMapOf<String, Long>() // <command identifier, until in millis>
    private val userCooldowns = mutableMapOf<Long, MutableMap<String, Long>>() // <user id, map<command identifier, until in millis>>

    init {
        synchronized(registry) {
            println("Registering commands...")
            registry.register()
            println("Done.")

            checks.add(Predicate {
                val cooldown: Double = if (it is SubCommandContext) {
                    if (it.currentSubCommand.cooldown) {
                        if (it.currentSubCommand.globalCooldown == 0.0) {
                            it.rootCommand.recursiveGlobalCooldown
                        } else {
                            it.currentSubCommand.globalCooldown
                        }
                    } else {
                        0.0
                    }
                } else {
                    it.rootCommand.globalCooldown
                }

                val identifier = if (it is SubCommandContext) "${it.rootCommand.name} ${it.currentSubCommand.name}" else it.rootCommand.name

                var setGlobal: Boolean = false
                if (cooldown != 0.0) {
                    val current = cooldowns[identifier]
                    if (current != null) {
                        if (current > System.currentTimeMillis()) {
                            // TODO: Prettify current seconds
                            val remaining = (current.toLong() - System.currentTimeMillis())
                            println(System.currentTimeMillis().toString() + ", " + current.toLong() + ", " + remaining)
                            it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl).setTitle("Cooldown", null)
                                    .setDescription("That command is on global cooldown for ${prettify(remaining)}."))
                            return@Predicate false
                        }
                    }
                    setGlobal = true
                }

                // Global cooldown passed.
                val annoUser: Double = if (it is SubCommandContext) {
                    if (it.currentSubCommand.cooldown) {
                        if (it.currentSubCommand.userCooldown == 0.0) {
                            it.rootCommand.recursiveUserCooldown
                        } else {
                            it.currentSubCommand.userCooldown
                        }
                    } else {
                        0.0
                    }
                } else {
                    it.rootCommand.userCooldown
                }

                if (annoUser != 0.0) {
                    val user = userCooldowns[it.author.idLong] ?: mutableMapOf() // All users should have one as long as it isnt empty.
                    val userCooldown = user[identifier]
                    if (userCooldown != null) {
                        if (userCooldown > System.currentTimeMillis()) {
                            val remaining = (userCooldown.toLong() - System.currentTimeMillis())
                            it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl).setTitle("Cooldown", null)
                                    .setDescription("That command is on cooldown for ${prettify(remaining)}."))
                            return@Predicate false
                        }
                    }
                    user[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(annoUser.toLong())
                    userCooldowns[it.author.idLong] = user
                }
                if (setGlobal) {
                    cooldowns[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(cooldown.toLong())
                }

                return@Predicate true
            })
        }
    }

    fun prettify(timeDiff: Long): String { //I just got this from an old project of mine, I'll prettify it later
        val second = timeDiff / 1000 % 60
        val minute = timeDiff / (1000 * 60) % 60
        val hour = timeDiff / (1000 * 60 * 60) % 24
        val day = timeDiff / (1000 * 60 * 60 * 24)

        if (day > 0) return "$day${ensurePlural(day, "day")} and $hour${ensurePlural(hour, "hour")}"
        if (hour > 0) return "$hour${ensurePlural(hour, "hour")} and $minute${ensurePlural(minute, "minute")}"
        if (minute > 0) return "$minute${ensurePlural(minute, "minute")} and $second${ensurePlural(second, "second")}"
        if (second > 0) return "$second${ensurePlural(second, "second")}"
        return timeDiff.toString() + "ms"
    }

    fun ensurePlural(numb: Long, str: String): String {
        return if (numb > 1) " ${str}s" else " $str"
    }

    override fun onGenericEvent(event: Event) {
        if (registry.jdalisteners.containsKey(event::class.java)) {
            registry.jdalisteners[event::class.java]!!.forEach {
                it.key.invoke(it.value.instance, event)
            }
        }

        super.onGenericEvent(event)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.message.rawContent.startsWith(commandPrefix)) {
            return
        }
        val command = event.message.rawContent.substring(commandPrefixLen)
        try {
            val (input, method, third) = registry[command, event]
            val (context, clazz) = third
            if (method == null || context == null || clazz == null) {
                event.message.addReaction("❔").queue() //dats better
                return
            }

            event.message.delete().queue()
            if (checks.any { !it.test(context) }) {
                return
            }

            method.invoke(clazz, context)
        } catch (ex: Exception) {
            ex.printStackTrace()
            event.channel.sendMessage("* An error occured while trying to handle that command. Please ask Project Coalesce developers to look at the error.").queue()
        }
    }
}

class CommandRegistry internal constructor() {
    val commands = mutableMapOf<String, CommandEntry>()
    val subcommands = mutableMapOf<CommandEntry, MutableList<String>>()
    val jdalisteners = mutableMapOf<Class<Event>, MutableMap<Method, CommandEntry>>()

    internal fun register() {
        val classes = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner())
                .setUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(FilterBuilder().include(FilterBuilder.prefix("com.coalesce.bot.commands.executors"))))
                .getSubTypesOf(Object::class.java).filter { !it.name.contains('$') }
        for (clazz in classes) {
            process(clazz)
        }
    }

    private fun process(clazz: Class<*>) {
        val commandEntry = CommandEntry(clazz)
        subcommands[commandEntry] = mutableListOf()

        commands[commandEntry.rootAnnotation.name.replace(" ", "").toLowerCase()] = commandEntry
        if (commandEntry.rootAnnotation.aliases.isNotEmpty()) {
            commandEntry.rootAnnotation.aliases.map { it.toLowerCase().replace(" ", "") }.forEach {
                commands[it] = commandEntry
            }
        }
        commandEntry.subcommands.map { it.key }.forEach {
            subcommands[commandEntry]!!.add(it.toLowerCase())
        }
        commandEntry.jdalisteners.forEach { entry ->
            val map = mutableMapOf<Method, CommandEntry>()
            entry.value.forEach { map.put(it, commandEntry) }
               if(!jdalisteners.containsKey(entry.key)) jdalisteners.put(entry.key, mutableMapOf<Method, CommandEntry>())
            jdalisteners[entry.key]!!.putAll(map)
        }
    }

    operator fun get(
            command: String,
            event: MessageReceivedEvent
    ): Triple<String, Method?, Pair<CommandContext?, Any?>> {
        val split = command.split(" ")
        val jda = event.jda
        val args: Array<String>
        val command = commands[split[0].toLowerCase()] ?: return Triple(split[0], null, null to null)

        if (split.size >= 2) {
            if (subcommands[command]!!.contains(split[1])) {
                args =
                if (split.size > 2) {
                    Arrays.copyOfRange(split.toTypedArray(), 2, split.size)
                } else {
                    arrayOf()
                }
                return Triple(split[0] + " " + split[1], command.subcommands[split[1]]!!.first, SubCommandContext(jda, jda.selfUser, event.message, event, event.author, event.channel, command.rootAnnotation, command.subcommands, args, command.subcommands[split[1]]!!.second) to command.instance)
            }
        }
        if (split.size > 1) {
            args = Arrays.copyOfRange(split.toTypedArray(), 1, split.size)
        } else {
            args = arrayOf()
        }
        return Triple(split[0], command.rootMethod, RootCommandContext(jda, jda.selfUser, event.message, event, event.author, event.channel, command.rootAnnotation, command.subcommands, args) to command.instance)
    }
}

class CommandEntry(@Suppress("CanBeParameter") val clazz: Class<*>) {
    lateinit var rootMethod: Method
    lateinit var rootAnnotation: RootCommand
    @Suppress("DEPRECATION")
    val instance: Any = Main.instance.injector.getInstance(clazz)
    val subcommands = mutableMapOf<String, Pair<Method, SubCommand>>()
    val jdalisteners = mutableMapOf<Class<Event>, MutableList<Method>>()

    init {
        var setRoot = false
        for (method in clazz.declaredMethods) {
            val annotation = method.getAnnotation(RootCommand::class.java) ?: continue
            rootAnnotation = annotation
            rootMethod = method
            setRoot = true
            break
        }
        if (!setRoot) {
            throw RuntimeException("Couldn't find root command in ${clazz.name}")
        }
        for (method in clazz.declaredMethods) {
            val annotation = method.getAnnotation(SubCommand::class.java) ?: continue
            subcommands[annotation.name.replace(" ", "")] = method to annotation
            annotation.aliases.map { it.replace(" ", "") }.forEach { subcommands[it] = method to annotation }
        }
        for (method in clazz.declaredMethods) {
            method.getAnnotation(JDAListener::class.java) ?: continue
            val eventListener = method.parameterTypes.first() as Class<Event>
            if (!jdalisteners.containsKey(eventListener)) jdalisteners.put(eventListener, mutableListOf<Method>())
            jdalisteners[eventListener]!!.add(method)
        }
    }
}
