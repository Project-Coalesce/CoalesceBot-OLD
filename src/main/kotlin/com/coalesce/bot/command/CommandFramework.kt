package com.coalesce.bot.command

import com.coalesce.bot.Main
import com.coalesce.bot.commandPrefix
import com.coalesce.bot.commandPrefixLen
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.subList
import com.coalesce.bot.utilities.truncate
import com.google.inject.Injector
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.awt.Color
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

class Listener constructor(jda: JDA, adaptationArgsChecker: AdaptationArgsChecker, guice: Injector, pluginManager: PluginManager):
        ListenerAdapter(), Embeddables {
    val commandAliasMap = mutableMapOf<String, CommandFrameworkClass>()
    val eventHandlers = mutableMapOf<Class<*>, MutableList<Pair<Method, CommandFrameworkClass>>>()
    val reactionHandlers = mutableListOf<Pair<Method, CommandFrameworkClass.CommandInfo>>()
    val commands = mutableListOf<CommandFrameworkClass>()
    val checks = mutableListOf<(CommandContext, CommandFrameworkClass.CommandInfo) -> Boolean>(
            CooldownHandler()::cooldownCheck, ::permCheck
    )

    init {
        println("Registering commands...")
        val packages = mutableListOf("com.coalesce.bot.command.handlers")
        pluginManager.registeredPlugins.forEach { packages.addAll(it.pluginData.packagesScan) }

        val classes = mutableListOf<Class<*>>()
        classes.addAll(Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner())
                .setUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(FilterBuilder().apply {
                    packages.forEach { include(FilterBuilder.prefix(it)) }
                }))
                .getSubTypesOf(Object::class.java).filter { !it.name.contains('$') && !it.name.endsWith("Kt") })
        classes.addAll(pluginManager.addedCommands)
        classes.forEach { CommandFrameworkClass(this, adaptationArgsChecker, guice, it) }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.message.rawContent.startsWith(commandPrefix)) {
            return
        }
        if (event.channel !is TextChannel) {
            event.channel.sendMessage("Commands are not allowed in private messages.").queue()
            return
        }
        val command = event.message.rawContent.substring(commandPrefixLen)
        val split = command.split(" ")
        if (split.isEmpty()) {
             event.message.addReaction("❔").queue()
            return
        }
        val args = split.subList(1)
        val info = commandAliasMap[split.first().toLowerCase()] ?: run {
            event.message.addReaction("❔").queue()
            return
        }

        val context = CommandContext(
                event.author, event.message, event, info, args, Main.instance, event.channel as TextChannel
        )

        event.channel.sendTyping().queue()
        event.message.delete().queue()

        try {
            val (method, paramters, cmdInfo) = (info.botCommand(args, context) ?: run {
                context("Invalid argumentation.\nUsage:\n${info.commandInfo.usage}")
                return
            })
            context.info = cmdInfo
           // if (checks.any { !it(context, cmdInfo) }) return
            println(paramters.joinToString(separator = ", ") + " " + method.name)
            println("${method.parameterCount}, ${paramters.size}")
            method.invoke(info.instance, *(paramters.toTypedArray()))
        } catch (ex: Exception) {
            val thrw = if (ex is InvocationTargetException) ex.cause!! else ex

            if (thrw is ArgsException) {
                event.channel.sendMessage("${event.author.asMention} ❌: ${thrw.message}").queue()
                return
            }

            event.channel.sendMessage(embed().apply {
                setColor(Color(232, 46, 0))
                setTitle("Error", null)
                setDescription("An error occurred with that command.\n" +
                        "This has been reported to Coalesce developers.")
            }.build()).queue()
            System.err.println("An error occured while attempting to handle command '${command.truncate(0, 100)}' from ${event.author.name}")
            thrw.printStackTrace()
        }
    }

    fun register(command: CommandFrameworkClass) {
        commands.add(command)
        command.commandInfo.aliases.forEach {
            commandAliasMap[it.toLowerCase()] = command
        }
    }
}

class CommandFrameworkClass(
        commandHandler: Listener,
        adaptationArgsChecker: AdaptationArgsChecker,
        guice: Injector,
        clazz: Class<*>
) {
    lateinit var instance: Any
    lateinit var commandInfo: CommandInfo
    lateinit var botCommand: BotCommand

    init {
        if (clazz.isAnnotationPresent(Command::class.java)) {
            instance = guice.getInstance(clazz)
            commandHandler.commands.add(this)

            val info = CommandInfo()
            clazz.annotations.forEach {
                if (it is Command) {
                    info.name = it.name
                    info.aliases = it.aliases.replace("%name%", info.name).split(" ")
                } else if (it is Usage) info.usage = it.usage
                else if (it is GlobalCooldown) info.globalCooldown = it.globalCooldownUnit.toMillis(it.globalCooldown)
                else if (it is UserCooldown) info.userCooldown = it.userCooldownUnit.toMillis(it.userCooldown)
            }

            val methods = mutableMapOf<Array<Class<*>>, Method>()
            val subCommands = mutableMapOf<String, Pair<MutableMap<Array<Class<*>>, Method>, CommandInfo>>()

            clazz.declaredMethods.forEach {
                if (it.isAnnotationPresent(CommandAlias::class.java)) {
                    methods[getClassArray(it)] = it
                // Sub Commands
                } else if (it.isAnnotationPresent(SubCommand::class.java)) {
                    val subCommandAnno = it.getAnnotationsByType(SubCommand::class.java).first()

                    val map = subCommands[subCommandAnno.name] ?: mutableMapOf<Array<Class<*>>, Method>() to CommandInfo(subCommand = true)
                    map.first[getClassArray(it)] = it
                    it.annotations.forEach {
                        if (it is Usage) map.second.usage = it.usage
                        else if (it is GlobalCooldown) map.second.globalCooldown = it.globalCooldownUnit.toMillis(it.globalCooldown)
                        else if (it is UserCooldown) map.second.userCooldown = it.userCooldownUnit.toMillis(it.userCooldown)
                    }
                    subCommands[subCommandAnno.name] = map
                } else if (it.isAnnotationPresent(SubCommandAlias::class.java)) {
                    val subCommandAnno = it.getAnnotationsByType(SubCommandAlias::class.java).first()
                    val map = subCommands[subCommandAnno.name] ?: mutableMapOf<Array<Class<*>>, Method>() to CommandInfo(subCommand = true)
                    map.first[getClassArray(it)] = it
                    subCommands[subCommandAnno.name] = map
                // Listeners
                } else if (it.isAnnotationPresent(JDAListener::class.java)) {
                    commandHandler.eventHandlers[it.parameterTypes[0]] =
                            (commandHandler.eventHandlers[it.parameterTypes[0]] ?: mutableListOf()).apply {
                                add(it to this@CommandFrameworkClass)
                            }
                } else if (it.isAnnotationPresent(ReactionListener::class.java)) {
                    val reactionInfo = CommandInfo(name = it.getAnnotationsByType(ReactionListener::class.java).first().name)

                    it.annotations.forEach {
                        if (it is Usage) reactionInfo.usage = it.usage
                        else if (it is GlobalCooldown) reactionInfo.globalCooldown = it.globalCooldownUnit.toMillis(it.globalCooldown)
                        else if (it is UserCooldown) reactionInfo.userCooldown = it.userCooldownUnit.toMillis(it.userCooldown)
                    }

                    commandHandler.reactionHandlers.add(it to reactionInfo)
                }
            }

            // Pretty usage generator
            if (info.usage.isEmpty()) {
                info.usage = StringBuilder().apply {
                    methods.forEach { append("/${info.name.toLowerCase()} " + it.key.joinToString(separator = " ") { "<${it.simpleName}>" }) }
                }.toString()
            }
            commandInfo = info

            // BOT COMMAND
            val subBotCommands = mutableMapOf<String, BotCommand>()
            subCommands.forEach { subBotCommands[it.key] = BotCommand(it.value.first, mapOf(), adaptationArgsChecker, it.value.second) }

            botCommand = BotCommand(methods, subBotCommands, adaptationArgsChecker, commandInfo)
            commandHandler.register(this)

        }
    }

    private fun getClassArray(method: Method) =
            if (method.parameterTypes.isNotEmpty() && method.parameterTypes.first() == CommandContext::class.java)
                method.parameterTypes.toList().subList(1).toTypedArray()
            else method.parameterTypes

    data class CommandInfo(
            var name: String = "",
            var aliases: List<String> = listOf(),
            var usage: String = "",
            var userCooldown: Long = 0L,
            var globalCooldown: Long = 0L,
            var subCommand: Boolean = false
    )
}

class BotCommand(
        val methods: Map<Array<Class<*>>, Method>,
        val subCommands: Map<String, BotCommand>,
        val commandTypeAdapter: AdaptationArgsChecker,
        private val commandInfo: CommandFrameworkClass.CommandInfo
) {
    operator fun invoke(args: List<String>, context: CommandContext): Triple<Method, List<Any>, CommandFrameworkClass.CommandInfo>? {
        if (args.isNotEmpty() && subCommands.containsKey(args.first())) {
            return subCommands[args.first()]!!(args.subList(1), context)
        } else {
            methods.forEach {
                if (args.size < it.key.size) return@forEach
                var arguments = args.toTypedArray()
                val paramters = mutableListOf<Any>(context)
                it.key.forEachIndexed { _, it ->
                    val (newArgs, obj) = commandTypeAdapter.adapt(arguments, it) ?: return@forEach
                    arguments = newArgs; paramters.add(obj)
                }
                return@invoke Triple(it.value, paramters, commandInfo)
            }

            return null
        }
    }
}

class CommandContext(
        val author: User,
        val message: Message,
        val event: MessageReceivedEvent,
        val command: CommandFrameworkClass,
        val args: List<String>,
        val main: Main,
        val channel: TextChannel,
        val guild: Guild = channel.guild
) {
    lateinit var info: CommandFrameworkClass.CommandInfo
    fun mention(text: String) = invoke(author, text)

    val mentioned: User
        get() = message.mentionedUsers.firstOrNull() ?: throw ArgsException("You must mention someone.")

    operator fun invoke(text: String, mentionUser: Boolean = true, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) {
        val task = channel.sendMessage(if (mentionUser) "${author.asMention}: $text" else text)
        val whenDo: (Message) -> Unit = {
            if (deleteAfter != null) it.delete().queueAfter(deleteAfter.first, deleteAfter.second)
            if (handler != null) handler(it)
        }

        if (queueAfter != null) task.queueAfter(queueAfter.first, queueAfter.second, whenDo)
        else task.queue(whenDo)
    }

    operator fun invoke(mention: IMentionable, text: String, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) {
        val task = channel.sendMessage("${mention.asMention}: $text")
        val whenDo: (Message) -> Unit = {
            if (deleteAfter != null) it.delete().queueAfter(deleteAfter.first, deleteAfter.second)
            if (handler != null) handler(it)
        }

        if (queueAfter != null) task.queueAfter(queueAfter.first, queueAfter.second, whenDo)
        else task.queue(whenDo)
    }

    operator fun invoke(embed: MessageEmbed, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) {
        val task = channel.sendMessage(embed)
        val whenDo: (Message) -> Unit = {
            if (deleteAfter != null) it.delete().queueAfter(deleteAfter.first, deleteAfter.second)
            if (handler != null) handler(it)
        }

        if (queueAfter != null) task.queueAfter(queueAfter.first, queueAfter.second, whenDo)
        else task.queue(whenDo)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Command(
        val name: String,
        val aliases: String = "%%name%%"
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class UserCooldown(
        val userCooldown: Long,
        val userCooldownUnit: TimeUnit = TimeUnit.SECONDS
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class GlobalCooldown(
        val globalCooldown: Long,
        val globalCooldownUnit: TimeUnit = TimeUnit.SECONDS
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Usage(val usage: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandAlias

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubCommand(
        val name: String,
        val aliases: String = "%%name%%"
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubCommandAlias(
        val name: String
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class JDAListener

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ReactionListener(val name: String)

class ArgsException(message: String): Exception(message)