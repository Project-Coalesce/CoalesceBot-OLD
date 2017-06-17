package com.coalesce.bot.commands

import com.coalesce.bot.*
import com.coalesce.bot.permissions.RankManager
import com.coalesce.bot.utilities.truncate
import com.coalesce.bot.utilities.tryLog
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
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
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.TimeUnit

class Listener internal constructor(val jda: JDA) : ListenerAdapter(), Embeddables {
    val registry = CommandRegistry()
    val checks = mutableSetOf<(CommandContext) -> Boolean>()
    val perms = RankManager(jda)
    val cooldowns = mutableMapOf<String, Long>() // <command identifier, until in millis>
    val userCooldowns = mutableMapOf<Long, MutableMap<String, Long>>() // <user id, map<command identifier, until in millis>>
    val cooldown = CooldownCheck(this)
    private val blacklist: MutableMap<Long, String>
    private val welcomeMessage = "Welcome, %s, to the Coalesce Coding Discord server!\n" +
            "If you are able to code in an language and would like to have a fancy color for it, use !request <rank>.\n" +
            "The currently supported languages include Java, Kotlin, Web, Spigot and Python.\n" +
            "Follow the rules at %s and enjoy your stay!"

    init {
        if (blacklistFile.exists()) blacklist = gson.fromJson(blacklistFile.readText(), object: TypeToken<MutableMap<Long, String>>() {}.type)
        else blacklist = mutableMapOf()
    }

    fun register() {
        synchronized(registry) {
            println("Registering commands...")
            registry.register()
            println("Done.")

            checks.add({
                val isBlacklisted = isBlacklisted(it.author)

                if (isBlacklisted) {
                    it(embed().apply{
                        setColor(Color(204, 36, 24))
                        setAuthor(it.message.author.name, null, it.message.author.avatarUrl)
                        setTitle("You are blacklisted from using CoalesceBot.", null)
                        addField("Reason", blacklist[it.message.author.idLong], false)
                    }, { delete().queueAfter(10L, TimeUnit.SECONDS) })
                }

                !isBlacklisted
            })
            checks.add(cooldown::cooldownCheck)
            checks.add({
                val permissable = it.channel.idLong == 315934590109745154 || perms.hasPermission(it.message.guild.getMember(it.message.author), it.rootCommand.permission)

                if (!permissable) {
                    it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl)
                            .setTitle("Permission", null)
                            .setDescription("<:no_permission:315617783738007552> You are not permitted to run that command."),
                            { delete().queueAfter(5L, TimeUnit.SECONDS) })
                }

                permissable
            })
        }
    }

    fun isBlacklisted(user: User): Boolean = blacklist.containsKey(user.idLong)
    fun reason(user: User): String? = blacklist[user.idLong]

    fun blacklist(user: User, reason: String) {
        blacklist[user.idLong] = reason
        blacklistSave()
    }

    fun unblacklist(user: User) {
        blacklist.remove(user.idLong)
        blacklistSave()
    }

    private fun blacklistSave() {
        if (blacklistFile.exists()) blacklistFile.delete()
        if (!blacklistFile.parentFile.exists()) blacklistFile.parentFile.mkdirs()
        blacklistFile.createNewFile()
        blacklistFile.writeText(gson.toJson(blacklist))
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        event.guild.publicChannel.sendMessage(String.format(welcomeMessage, event.member.asMention, "<#269178364483338250>")).queue()
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        event.guild.publicChannel.sendMessage("Today, we see ${event.member.effectiveName} leave us <:feelsbad:302954104718884875>").queue()
    }

    override fun onGenericEvent(event: Event) {
        if (registry.jdalisteners.containsKey(event::class.java)) {
            registry.jdalisteners[event::class.java]!!.forEach {
                try {
                    if (it.key.parameterCount == 2) it.key.invoke(it.value.instance, event, EventContext(this, jda, it.value.rootAnnotation))
                    else it.key.invoke(it.value.instance, event)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        super.onGenericEvent(event)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        /*if (event.message.isMentioned(jda.selfUser)) {
            //if (chatbot.isDisabled) return

            //getChatbotMessage(event.message, jda).apply { event.channel.sendMessage("${event.message.author.asMention}: " +
            //if(this!!.isEmpty()) "* Failed to find message" else this).queue() }

        }*/
        if (!event.message.rawContent.startsWith(commandPrefix)) {
            return
        }
        val command = event.message.rawContent.substring(commandPrefixLen)
        try {
            val (input, method, third) = registry[command, event]
            val (context, clazz) = third
            if (method == null || context == null || clazz == null) {
                return
            }
            if (event.channel !is TextChannel) {
                event.channel.sendMessage("Commands are not allowed in private messages.").queue()
                return
            }

            event.channel.sendTyping().queue()
            event.message.delete().queue()
            if (checks.any { !it(context) }) {
                return
            }
            else if (context.rootCommand.type == CommandType.DEBUG
                     && event.channel.idLong != 315934590109745154L /** CoalesceBot #console id */) {
                return
            }

            method.invoke(clazz, context)
            cooldown.setCooldown(context, event.author)
        } catch (ex: Exception) {
            val thrw = if (ex is InvocationTargetException) ex.cause!! else ex

            if (thrw is ArgsException) {
                event.channel.sendMessage("${event.author.asMention} ❌: ${thrw.message}").queue()
                return
            }

            event.channel.sendMessage(embed().apply {
                setColor(Color(232, 46, 0))
                setTitle("Error", null)
                setDescription("An error occurred with that command:\n${thrw.javaClass.name}: ${thrw.message}\n" +
                            "Please report this to project coalesce developers.")
            }.build()).queue()
            System.err.println("An error occured while attempting to handle command '${command.truncate(0, 100)}' from ${event.author.name}")
            thrw.printStackTrace()
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
                .getSubTypesOf(Object::class.java).filter { !it.name.contains('$') && !it.name.endsWith("Kt") }
        val cmds = mutableListOf<CommandEntry>()

        for (clazz in classes) {
            tryLog("Failed to process ${clazz.name}") { cmds.add(process(clazz)) }
        }
        println("Loaded ${cmds.size} commands: " + cmds.joinToString(separator = ", ") { it.rootAnnotation.name })
    }

    private fun process(clazz: Class<*>): CommandEntry {
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

        return commandEntry
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
