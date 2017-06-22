package com.coalesce.bot.command

import com.coalesce.bot.Main
import com.coalesce.bot.command.handlers.Help
import com.coalesce.bot.commandPrefix
import com.coalesce.bot.commandPrefixLen
import com.coalesce.bot.utilities.*
import com.google.inject.Injector
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.GenericMessageEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.requests.RestAction
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.awt.Color
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.TimeUnit
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.kotlinFunction

class Listener constructor(jda: JDA, adaptationArgsChecker: AdaptationArgsChecker, guice: Injector, pluginManager: PluginManager):
        ListenerAdapter(), Embeddables {
    val commandAliasMap = mutableMapOf<String, CommandFrameworkClass>()
    val eventHandlers = mutableMapOf<Class<*>, MutableList<Pair<Method, CommandFrameworkClass>>>()
    val reactionHandlers = mutableListOf<Pair<Method, CommandFrameworkClass>>()
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
        Help.loadHelp(this)
        println("Registered ${commands.size} commands: ${commands.joinToString(separator = ", ") { it.commandInfo.name }.truncate(0, 1000)}")
    }

    override fun onGenericEvent(event: Event) {
        if (eventHandlers.containsKey(event.javaClass)) {
            eventHandlers[event.javaClass]!!.forEach {
                try {
                    it.first(it.second.instance, *(arrayOf(event)))
                } catch (ex: Exception) {
                    val thrw = if (ex is InvocationTargetException) ex.cause!! else ex
                    System.err.println("An error occured while attempting to handle event of type ${event.javaClass!!}")
                    thrw.printStackTrace()
                }
            }
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        val context = ReactionContext(event.user, event.messageIdLong, event, event.reactionEmote, Main.instance, event.channel, event.guild)
        reactionHandlers.forEach {
            it.first.invoke(it.second.instance, *(arrayOf(context)))
        }
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
            val (method, paramters, cmdInfo) = (info.botCommand(args, context, info.instance) ?: run {
                context(embed().apply {
                    embColor = Color(237, 45, 35)
                    embTitle = "Invalid Argumentation!"
                    field("Usage", "```${info.commandInfo.usage}```")
                })
                return
            })
            context.info = cmdInfo
            if (checks.any { !it(context, cmdInfo) }) return
            method.callBy(paramters)
        } catch (ex: Exception) {
            val thrw = if (ex is InvocationTargetException) ex.cause!! else ex

            if (thrw is ArgsException) {
                event.channel.sendMessage("${event.author.asMention} ❌: ${thrw.message}").queue()
                return
            }

            event.channel.sendMessage(embed().apply {
                embColor = Color(232, 46, 0)
                embTitle = "Error"
                description {
                    appendln("An error occured with that command.")
                    append("This has been reported to Coalesce developers.")
                }
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

            val subCommands = mutableMapOf<String, Pair<MutableMap<Pair<List<Class<*>>, List<KParameter>>, Pair<KCallable<*>, String>>, CommandInfo>>()
            val info = CommandInfo()
            clazz.annotations.forEach {
                if (it is Command) {
                    info.name = it.name
                    info.aliases = it.aliases.split(" ").toMutableList().apply { add(it.name) }
                } else if (it is Usage) info.usage = it.usage
                else if (it is GlobalCooldown) info.globalCooldown = it.globalCooldownUnit.toMillis(it.globalCooldown)
                else if (it is UserCooldown) info.userCooldown = it.userCooldownUnit.toMillis(it.userCooldown)
                else if (it is SubCommand) subCommands[it.name] = mutableMapOf<Pair<List<Class<*>>, List<KParameter>>, Pair<KCallable<*>, String>>() to CommandInfo(subCommand = true)
            }

            val methods = mutableMapOf<Pair<List<Class<*>>, List<KParameter>>, Pair<KCallable<*>, String>>()

            clazz.declaredMethods.forEach {
                if (Modifier.isStatic(it.modifiers)) return@forEach

                if (it.isAnnotationPresent(CommandAlias::class.java)) {
                    methods[getParameters(it)] = it.kotlinFunction!! to it.getAnnotationsByType(CommandAlias::class.java).first().description
                // Sub Commands
                } else if (it.isAnnotationPresent(SubCommandAlias::class.java)) {
                    val subCommandAnno = it.getAnnotationsByType(SubCommandAlias::class.java).first()
                    val map = subCommands[subCommandAnno.name]!!
                    map.first[getParameters(it)] = it.kotlinFunction!! to it.getAnnotationsByType(SubCommandAlias::class.java).first().description
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

                    commandHandler.reactionHandlers.add(it to this@CommandFrameworkClass)
                }
            }

            // Pretty usage generator
            if (info.usage.isEmpty()) {
                info.usage = StringBuilder().apply {
                    fun addMethod(method: Pair<List<Class<*>>, List<KParameter>>, name: String) =
                        appendln("$commandPrefix${name.toLowerCase()} " + method.second.subList(1).filter {
                            it.type.classifier != CommandContext::class }.joinToString(separator = " ") { if (it.isOptional) "[${it.name!!.capitalize()}]" else "<${it.name!!.capitalize()}>" })

                    methods.forEach { addMethod(it.key, info.name) }
                    subCommands.forEach { subCmd ->
                        subCmd.value.first.forEach { addMethod(it.key, subCmd.value.second.name) }
                    }
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

    private fun getParameters(method: Method): Pair<List<Class<*>>, List<KParameter>> {
        val params = method.kotlinFunction!!.parameters
        return method.parameterTypes.toList() to params
    }

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
        val methods: Map<Pair<List<Class<*>>, List<KParameter>>, Pair<KCallable<*>, String>>,
        val subCommands: Map<String, BotCommand>,
        val commandTypeAdapter: AdaptationArgsChecker,
        private val commandInfo: CommandFrameworkClass.CommandInfo
) {

    operator fun invoke(args: List<String>, context: CommandContext, instance: Any): Triple<KCallable<*>, Map<KParameter, Any>, CommandFrameworkClass.CommandInfo>? {
        if (args.isNotEmpty() && subCommands.containsKey(args.first())) {
            return subCommands[args.first()]!!(args.subList(1), context, instance)
        } else {
            methods.forEach {
                if (args.size < it.key.second.subList(2).count { !it.isOptional }) return@forEach
                var arguments = args.toTypedArray()
                val paramters = mutableMapOf(
                        it.key.second[0] to instance,
                        it.key.second[1] to context
                )

                if (args.isNotEmpty()) it.key.first.filter { it != CommandContext::class.java }.forEachIndexed { index, clazz ->
                    val kotlinParam = it.key.second[index + 2]
                    val (newArgs, obj) = commandTypeAdapter.adapt(arguments, clazz) ?:
                            run { if (kotlinParam.isOptional) return@forEachIndexed else return@forEach }
                    arguments = newArgs; paramters[kotlinParam] = obj
                }
                return@invoke Triple(it.value.first, paramters, commandInfo)
            }

            return null
        }
    }
}

fun MessageChannel.send(text: String, mention: IMentionable? = null, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) =
    sendTask(sendMessage(if (mention != null) "${mention.asMention}: $text" else text), deleteAfter, queueAfter, handler)

fun MessageChannel.send(embed: MessageEmbed, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) =
    sendTask(sendMessage(embed), deleteAfter, queueAfter, handler)

fun MessageChannel.send(embed: EmbedBuilder, mention: User, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) {
    embed.setAuthor(mention.name, null, mention.effectiveAvatarUrl)
    sendTask(sendMessage(embed.build()), deleteAfter, queueAfter, handler)
}

private fun sendTask(task: RestAction<Message>, deleteAfter: Pair<Long, TimeUnit>? = null,
                     queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) {
    val whenDo: (Message) -> Unit = {
        if (deleteAfter != null) it.delete().queueAfter(deleteAfter.first, deleteAfter.second)
        if (handler != null) handler(it)
    }

    if (queueAfter != null) task.queueAfter(queueAfter.first, queueAfter.second, whenDo)
    else task.queue(whenDo)
}

class ReactionContext(
        author: User,
        val message: Long,
        val event: GuildMessageReactionAddEvent,
        val emote: MessageReaction.ReactionEmote,
        main: Main,
        channel: TextChannel,
        guild: Guild = channel.guild
): Context(author, main, channel, guild)

class CommandContext(
        author: User,
        val message: Message,
        val event: MessageReceivedEvent,
        val command: CommandFrameworkClass,
        val args: List<String>,
        main: Main,
        channel: TextChannel,
        guild: Guild = channel.guild
): Context(author, main, channel, guild) {
    val mentioned: User
        get() = message.mentionedUsers.firstOrNull() ?: throw ArgsException("You must mention someone.")
}

open class Context(
        val author: User,
        val main: Main,
        val channel: TextChannel,
        val guild: Guild = channel.guild
) {
    lateinit var info: CommandFrameworkClass.CommandInfo
    fun mention(text: String) = invoke(author, text)
    fun usePCh(handler: PrivateChannel.() -> Unit) {
        if (author.hasPrivateChannel()) handler(author.privateChannel)
        else author.openPrivateChannel().queue(handler)
    }

    operator fun invoke(text: String, mentionUser: Boolean = true, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) =
        channel.send(text, if (mentionUser) author else null, deleteAfter, queueAfter, handler)

    operator fun invoke(mention: IMentionable, text: String, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) =
        channel.send(text, mention, deleteAfter, queueAfter, handler)

    operator fun invoke(embed: MessageEmbed, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) =
        channel.send(embed, deleteAfter, queueAfter, handler)

    operator fun invoke(embed: EmbedBuilder, deleteAfter: Pair<Long, TimeUnit>? = null,
                        queueAfter: Pair<Long, TimeUnit>? = null, handler: (Message.() -> Unit)? = null) =
        channel.send(embed, author, deleteAfter, queueAfter, handler)
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Command(
        val name: String,
        val aliases: String = ""
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
annotation class CommandAlias(val description: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubCommand(
        val name: String,
        val aliases: String = "%%name%%"
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubCommandAlias(
        val name: String,
        val description: String
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class JDAListener

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ReactionListener(val name: String)

class ArgsException(message: String): Exception(message)