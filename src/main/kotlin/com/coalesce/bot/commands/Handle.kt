package com.coalesce.bot.commands

import com.coalesce.bot.Main
import com.coalesce.bot.commandPrefix
import com.coalesce.bot.commandPrefixLen
import com.coalesce.bot.permissions.RankManager
import com.coalesce.bot.quotedFile
import com.coalesce.bot.utilities.tryLog
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
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
import java.util.function.Predicate

class Listener internal constructor(val jda: JDA) : ListenerAdapter(), Embeddables {
    val registry = CommandRegistry()
    val checks = mutableSetOf<Predicate<CommandContext>>()
    val perms = RankManager(jda)
    val cooldowns = mutableMapOf<String, Long>() // <command identifier, until in millis>
    val userCooldowns = mutableMapOf<Long, MutableMap<String, Long>>() // <user id, map<command identifier, until in millis>>
    private val welcomeMessage = "Welcome, %s, to the Coalesce Coding Discord server!\n" +
            "If you are able to code in an language and would like to have a fancy color for it, use !request <rank>.\n" +
            "The currently supported languages include Java, Kotlin, Web, Spigot and Python.\n" +
            "Follow the rules at %s and enjoy your stay!"

    fun register() {
        synchronized(registry) {
            println("Registering commands...")
            registry.register()
            println("Done.")

            checks.add(CooldownCheck(this))
            checks.add(Predicate {
                val permissable = it.channel.idLong == 315934590109745154 || perms.hasPermission(it.message.guild.getMember(it.message.author), it.rootCommand.permission)

                if (!permissable) {
                    it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl)
                            .setTitle("Permission", null)
                            .setDescription("<:no_permission:315617783738007552> You are not permitted to run that command."))
                }

                return@Predicate permissable
            })
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        event.guild.publicChannel.sendMessage(String.format(welcomeMessage, event.member.asMention, "<#269178364483338250>"))
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        event.guild.publicChannel.sendMessage("Today, we see ${event.member.effectiveName} leave us.")
    }

    override fun onGenericEvent(event: Event) {
        if (registry.jdalisteners.containsKey(event::class.java)) {
            registry.jdalisteners[event::class.java]!!.forEach {
                try {
                    it.key.invoke(it.value.instance, event)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        super.onGenericEvent(event)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.rawContent.startsWith(jda.selfUser.asMention)) {
            val quoted = event.message.rawContent.substring(jda.selfUser.asMention.length)
            quotedFile.writeText("\n$quoted")

            val response = "We're working on the chat bot. Try again later!"
            event.message.channel.sendMessage("${event.author.asMention}: $response")
        }

        if (!event.message.rawContent.startsWith(commandPrefix)) {
            return
        }
        val command = event.message.rawContent.substring(commandPrefixLen)
        var inputStr: String? = null

        try {
            val (input, method, third) = registry[command, event]
            inputStr = input
            val (context, clazz) = third
            if (method == null || context == null || clazz == null) {
                //event.message.addReaction("❔").queue()
                //I only removed it because proxi asked for it and he's such a nice guy
                return
            }

            event.message.delete().queue()
            if (checks.any { !it.test(context) }) {
                return
            }
            else if (context.rootCommand.type == CommandType.DEBUG
                     && event.channel.idLong != 315934590109745154L /** CoalesceBot #console id */) {
                return
            }

            method.invoke(clazz, context)
        } catch (ex: Exception) {
            event.channel.sendMessage(embed().setColor(Color(232, 46, 0)).setTitle("Error", null)
                    .setDescription("An error occurred with that command:\n${ex.javaClass.name}: ${ex.message}\n" +
                    "Please report this to project coalesce developers.").build())
            ex.printStackTrace()
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
            tryLog("Failed to process ${clazz.name}") { process(clazz) }
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
