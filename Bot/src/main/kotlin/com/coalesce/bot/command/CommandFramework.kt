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
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.requests.RestAction
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import java.awt.Color
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.kotlinFunction

class Listener constructor(jda: JDA, adaptationArgsChecker: AdaptationArgsChecker, guice: Injector, pluginManager: PluginManager):
        ListenerAdapter(), Embeddables {
    val commandAliasMap = mutableMapOf<String, CommandFrameworkClass>()
    val eventHandlers = mutableMapOf<Class<*>, MutableList<Pair<Method, Any>>>()
    val reactionHandlers = mutableListOf<ReactionHandler>()
    val commands = mutableListOf<CommandFrameworkClass>()
    val cooldownHandler = CooldownHandler()
    val checks = mutableListOf<(Context) -> Boolean>(
            cooldownHandler::cooldownCheck, ::permCheck
    )

    init {
        println("Registering commands...")
        val classLoaders = mutableListOf(javaClass.classLoader as URLClassLoader)
        val packages = mutableListOf("com.coalesce.bot.command.handlers")
        pluginManager.registeredPlugins.forEach {
            classLoaders.add(it.pluginClassLoader)
            packages.addAll(it.pluginData.packagesScan)
        }

        val urls = mutableListOf<URL>()
        classLoaders.forEach { urls.addAll(it.urLs) }

        val classes = mutableListOf<Class<*>>()
        classes.addAll(Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner())
                .setUrls(urls)
                .addClassLoaders(*(classLoaders.toTypedArray()))
                .filterInputsBy { !it!!.endsWith("Kt") && !it.contains('$') && packages.any { pck -> it.startsWith(pck) } })
                .getSubTypesOf(Object::class.java).filter { !it.name.contains('$') && !it.name.endsWith("Kt") })
        classes.addAll(pluginManager.addedCommands)
        classes.forEach { tryLog("Failed to register command class at ${it.name}") {
            if (it.isAnnotationPresent(Command::class.java)) CommandFrameworkClass(this, adaptationArgsChecker, guice, it)
            else if (it.isAnnotationPresent(JDAEventHandler::class.java)) {
                val instance = guice.getInstance(it)
                it.declaredMethods.forEach {
                    if (it.isAnnotationPresent(JDAListener::class.java)) {
                        val clazz = it.parameterTypes.first()
                        eventHandlers[clazz] = (eventHandlers[clazz] ?: mutableListOf()).apply { add(it to instance) }
                    }
                }
            }
        } }
        Help.loadHelp(this)
        println("Registered ${commands.size} commands: ${commands.joinToString(separator = ", ") { it.commandInfo.name }.truncate(0, 1000)}")
    }

    override fun onGenericEvent(event: Event) {
        if (eventHandlers.containsKey(event.javaClass)) {
            eventHandlers[event.javaClass]!!.forEach {
                try {
                    it.first(it.second, *(arrayOf(event)))
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
            if ((checks and it.checks).any { c -> !c(context) })
                try{
                    it.method.invoke(it.clazz.instance, *(arrayOf(context)))
                } catch (ex: Exception) {
                    val thrw = if (ex is InvocationTargetException) ex.cause!! else ex

                    if (thrw is ArgsException) {
                        event.channel.sendMessage("${event.user.asMention} ❌: ${thrw.message}").queue()
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
                    System.err.println("An error occured while attempting to handle reaction from ${event.user.name}")
                    thrw.printStackTrace()
                }
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
            if(!info.botCommand(cooldownHandler, args, context, info.instance, checks)) {
                context(embed().apply {
                    embColor = Color(237, 45, 35)
                    embTitle = "Invalid Argumentation!"
                    field("Usage", "```${info.commandInfo.usage}```")
                })
            }
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
    val instance = guice.getInstance(clazz)!!
    val commandInfo: CommandInfo
    val botCommand: BotCommand

    init {
        val subCommands = mutableMapOf<String, Pair<MutableList<UsableMethod>, CommandInfo>>()
        fun newSubCommandMap() = mutableListOf<UsableMethod>() to CommandInfo(subCommand = true)
        val info = CommandInfo()
        clazz.annotations.forEach {
            if (it is Command) {
                info.name = it.name
                info.aliases = it.aliases.split(" ").toMutableList().apply { add(it.name) }
            }
            else if (it is GlobalCooldown) info.globalCooldown = it.globalCooldownUnit.toMillis(it.globalCooldown)
            else if (it is UserCooldown) info.userCooldown = it.userCooldownUnit.toMillis(it.userCooldown)
        }

        val methods = mutableListOf<UsableMethod>()

        clazz.declaredMethods.forEach {
            if (Modifier.isStatic(it.modifiers)) return@forEach

            if (it.isAnnotationPresent(CommandAlias::class.java)) {
                methods.add(UsableMethod.of(it, it.getAnnotationsByType(CommandAlias::class.java).first().description))
                // Sub Commands
            } else if (it.isAnnotationPresent(SubCommand::class.java)) {
                val subCommandAnno = it.getAnnotationsByType(SubCommand::class.java).first()
                val map = subCommands[subCommandAnno.name] ?: newSubCommandMap()
                map.second.aliases = subCommandAnno.aliases.split(" ").toMutableList().apply { add(subCommandAnno.name) }
                map.first.add(UsableMethod.of(it, subCommandAnno.description))
                it.declaredAnnotations.forEach {
                    if (it is Usage) map.second.usage = it.usage
                    else if (it is GlobalCooldown) map.second.globalCooldown = it.globalCooldownUnit.toMillis(it.globalCooldown)
                    else if (it is UserCooldown) map.second.userCooldown = it.userCooldownUnit.toMillis(it.userCooldown)
                }
                subCommands[subCommandAnno.name] = map
            } else if (it.isAnnotationPresent(SubCommandAlias::class.java)) {
                val subCommandAnno = it.getAnnotationsByType(SubCommandAlias::class.java).first()
                val map = subCommands[subCommandAnno.name] ?: newSubCommandMap()
                map.first.add(UsableMethod.of(it, it.getAnnotationsByType(SubCommandAlias::class.java).first().description))
                subCommands[subCommandAnno.name] = map
                // Listeners
            } else if (it.isAnnotationPresent(JDAListener::class.java)) {
                commandHandler.eventHandlers[it.parameterTypes[0]] =
                        (commandHandler.eventHandlers[it.parameterTypes[0]] ?: mutableListOf()).apply {
                            add(it to instance)
                        }
            } else if (it.isAnnotationPresent(ReactionListener::class.java)) {
                val anno = it.getAnnotationsByType(ReactionListener::class.java).first()
                val reactionInfo = CommandInfo(name = anno.name)

                it.annotations.forEach {
                    if (it is Usage) reactionInfo.usage = it.usage
                    else if (it is GlobalCooldown) reactionInfo.globalCooldown = it.globalCooldownUnit.toMillis(it.globalCooldown)
                    else if (it is UserCooldown) reactionInfo.userCooldown = it.userCooldownUnit.toMillis(it.userCooldown)
                }

                commandHandler.reactionHandlers.add(ReactionHandler(it, this@CommandFrameworkClass, reactionInfo, anno.extraChecks.map {
                    val meth = clazz.getDeclaredMethod(it, *(arrayOf(ReactionContext::class.java)))
                    Predicate<ReactionContext> { meth(it) as Boolean }
                }.map(Predicate<ReactionContext>::toLambdaFunc)))
            }
        }

        info.usage = StringBuilder().apply {
            fun addMethod(method: UsableMethod, name: String) =
                    appendln("$commandPrefix${name.toLowerCase()} ${method.info}: ${method.usage}")

            methods.forEach { addMethod(it, info.name) }
            subCommands.forEach { subCmd ->
                subCmd.value.first.forEach { addMethod(it, subCmd.value.second.name) }
            }
        }.toString()
        commandInfo = info

        // BOT COMMAND
        val subBotCommands = mutableMapOf<String, BotCommand>()
        subCommands.forEach {
            val command = BotCommand(it.value.first, mapOf(), adaptationArgsChecker, it.value.second)
            it.value.second.aliases.forEach { subBotCommands[it.toLowerCase()] = command }
        }

        botCommand = BotCommand(methods, subBotCommands, adaptationArgsChecker, commandInfo)
        commandHandler.register(this)
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

data class ReactionHandler(
        val method: Method,
        val clazz: CommandFrameworkClass,
        val info: CommandFrameworkClass.CommandInfo,
        val checks: List<(ReactionContext) -> Boolean>
)

class KotlinUsableMethod(
        val kParams: List<KParameter>,
        val kCallable: KCallable<*>,
        classList: List<Parameter>,
        javaCallable: Method,
        info: String,
        usage: String =
        if (javaCallable.isAnnotationPresent(Usage::class.java)) javaCallable.getAnnotation(Usage::class.java).usage
        else kParams.subList(2).joinToString(separator = " ") { if (it.isOptional) "[${it.name!!.capitalize()}]" else "<${it.name!!.capitalize()}>" }
): UsableMethod(classList, javaCallable, info, usage) {
    override val paramCount = kParams.subList(2).count { !it.isOptional }
}

open class UsableMethod(
        val classList: List<Parameter>,
        val invoke: Method,
        val usage: String,
        val info: String
) {
    companion object {
        fun of(meth: Method, info: String): UsableMethod {
            if (meth.kotlinFunction != null) {
                val kfunc = meth.kotlinFunction!!
                return KotlinUsableMethod(kfunc.parameters, kfunc, meth.parameters.toList(), meth, info)
            }
            return UsableMethod(meth.parameters.toList(), meth, info, meth.getAnnotation(Usage::class.java)!!.usage)
        }
    }

    open val paramCount = classList.size
}

class BotCommand(
        val methods: List<UsableMethod>,
        val subCommands: Map<String, BotCommand>,
        val commandTypeAdapter: AdaptationArgsChecker,
        private val commandInfo: CommandFrameworkClass.CommandInfo
) {

    operator fun invoke(cooldownHandler: CooldownHandler, args: List<String>, context: CommandContext, instance: Any, checks: List<(Context) -> Boolean>): Boolean {
        if (args.isNotEmpty() && subCommands.containsKey(args.first().toLowerCase())) {
            return subCommands[args.first().toLowerCase()]!!(cooldownHandler, args.subList(1), context, instance, checks)
        } else {
            methods.forEach {
                if (args.size < it.paramCount) return@forEach
                var arguments = args.toTypedArray()
                if (it is KotlinUsableMethod) {
                    val paramters = mutableMapOf(
                            it.kParams[0] to instance,
                            it.kParams[1] to context
                    )

                    if (args.isNotEmpty()) it.classList.filter { it.type != CommandContext::class.java }.forEachIndexed { index, parameter ->
                        val kotlinParam = it.kParams[index + 2]
                        if (parameter.isAnnotationPresent(VarArg::class.java)) {
                            if (parameter.type == String::class.java) paramters[kotlinParam] = arguments.joinToString(separator = " ")
                            else paramters[kotlinParam] = listOf<Any> {
                                while (arguments.isNotEmpty()) {
                                    val (newArgs, obj) = commandTypeAdapter.adapt(arguments, parameter.type) ?: break
                                    arguments = newArgs
                                    add(obj)
                                }
                            }
                            return@forEachIndexed
                        }

                        val (newArgs, obj) = commandTypeAdapter.adapt(arguments, parameter.type) ?:
                                run { if (kotlinParam.isOptional) return@forEachIndexed else return@forEach }
                        arguments = newArgs; paramters[kotlinParam] = obj
                    }

                    context.info = commandInfo
                    if (checks.any { !it(context) }) return@invoke true
                    it.kCallable.callBy(paramters)
                    if (commandInfo.userCooldown > 0L) cooldownHandler.doUserCooldown(context.author, commandInfo)
                    if (commandInfo.globalCooldown > 0L) cooldownHandler.doGlobalCooldown(commandInfo)
                    return@invoke true
                }

                val objects = mutableListOf<Any?>()
                if (args.isNotEmpty()) it.classList.filter { it.type != CommandContext::class.java }.forEachIndexed { index, parameter ->
                    if (parameter.isAnnotationPresent(VarArg::class.java)) {
                        if (parameter.type == String::class.java) objects.add(arguments.joinToString(separator = " "))
                        else objects.add(listOf<Any> {
                            var argset = args.subList(index).toTypedArray()
                            while (argset.isNotEmpty()) {
                                val (newArgs, obj) = commandTypeAdapter.adapt(arguments, parameter.type) ?: break
                                argset = newArgs
                                add(obj)
                            }
                        })
                        return@forEachIndexed
                    }

                    val (newArgs, obj) = commandTypeAdapter.adapt(arguments, parameter.type) ?:
                            if (parameter.isAnnotationPresent(Optional::class.java)) {
                                objects.add(null)
                                return@forEachIndexed
                            } else return@forEach
                    arguments = newArgs; objects.add(obj)
                }

                context.info = commandInfo
                if (checks.any { !it(context) }) return@invoke true
                it.invoke(instance, *(objects.toTypedArray()))
                if (commandInfo.userCooldown > 0L) cooldownHandler.doUserCooldown(context.author, commandInfo)
                if (commandInfo.globalCooldown > 0L) cooldownHandler.doGlobalCooldown(commandInfo)
            }

            return false
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

fun User.usePCh(handler: PrivateChannel.() -> Unit) {
    if (hasPrivateChannel()) handler(privateChannel)
    else openPrivateChannel().queue(handler)
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

    fun usePCh(handler: PrivateChannel.() -> Unit) = author.usePCh(handler)

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
annotation class JDAEventHandler

@Retention(AnnotationRetention.RUNTIME)
annotation class UserCooldown(
        val userCooldown: Long,
        val userCooldownUnit: TimeUnit = TimeUnit.SECONDS
)

@Retention(AnnotationRetention.RUNTIME)
annotation class GlobalCooldown(
        val globalCooldown: Long,
        val globalCooldownUnit: TimeUnit = TimeUnit.SECONDS
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Usage(val usage: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CommandAlias(val description: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubCommand(
        val name: String,
        val aliases: String = "%%name%%",
        val description: String
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
annotation class VarArg

@Retention(AnnotationRetention.RUNTIME)
annotation class Optional

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ReactionListener(
        val name: String,
        val extraChecks: Array<String>
)

class ArgsException(message: String): Exception(message)