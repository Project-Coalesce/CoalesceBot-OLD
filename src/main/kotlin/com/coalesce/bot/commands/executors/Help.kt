package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.ifwithDo
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.PrivateChannel
import java.awt.Color
import java.nio.file.Files.delete
import java.util.*
import java.util.concurrent.TimeUnit

class Help @Inject constructor(val bot: Main) : Embeddables {
    private val message = embed().apply {
        setColor(Color(0xBE58B6))

        val map = mutableMapOf<CommandType, MutableSet<CommandEntry>>()
        bot.listener.registry.commands.forEach { _, value ->
            if (!value.rootAnnotation.type.hidden) {
                map[value.rootAnnotation.type] = map[value.rootAnnotation.type]?.apply { add(value) } ?: mutableSetOf()
            }
        }

        map.forEach { type, entries ->
            addField(type.name.toLowerCase().capitalize(), StringBuilder().apply {
                entries.forEach {
                    append("`${if (it.rootAnnotation.name == "xD") "xD" else it.rootAnnotation.name.capitalize()}`")
                    append(" ${it.rootAnnotation.usage} ")
                    if (it.rootAnnotation.aliases.isNotEmpty()) {
                        append("[${it.rootAnnotation.aliases.map { it.capitalize() }.joinToString(separator = ", ")}]")
                    }
                    append(": ${it.rootAnnotation.description}\n")
                }
            }.toString(), false)
        }
    }.build()

    @RootCommand(
            name = "Help",
            permission = "commands.help",
            description = "Displays all the available commands.",
            aliases = arrayOf("?", "h"),
            userCooldown = 35.0
    )
    fun execute(context: RootCommandContext) {
        fun send(pm: PrivateChannel) {
            pm.sendMessage(message).queue()
            context(context.author, "A list of commands was sent into your private messages.") { delete().queueAfter(20, TimeUnit.SECONDS) }
        }

        if (context.author.hasPrivateChannel()) send(context.author.privateChannel)
        else context.author.openPrivateChannel().queue(::send)
    }
}