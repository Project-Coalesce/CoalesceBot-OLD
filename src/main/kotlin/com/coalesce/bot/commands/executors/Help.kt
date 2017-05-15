package com.coalesce.bot.commands.executors

import com.coalesce.bot.Colour
import com.coalesce.bot.Main
import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.ifwithDo
import com.google.inject.Inject
import java.util.*
import java.util.concurrent.TimeUnit

class Help @Inject constructor(val bot: Main) : Embeddables {
    @RootCommand(
            name = "Help",
            permission = "commands.help",
            description = "Displays all the available commands.",
            aliases = arrayOf("?", "h"),
            globalCooldown = 35.0
    )
    fun execute(context: RootCommandContext) {
        val embed = embed().data("Help", colour = Colour.GREEN)
        val map = mutableMapOf<CommandType, MutableSet<CommandEntry>>()
        bot.listener.registry.commands.forEach { _, value ->
            if (value.rootAnnotation.type != CommandType.HIDDEN) {
                map[value.rootAnnotation.type] = map[value.rootAnnotation.type]?.apply { add(value) } ?: mutableSetOf()
            }
        }
        val out = mutableMapOf<CommandType, String>()
        map.forEach { type, entries ->
            val builder = StringBuilder("```\n")
            entries.forEach {
                builder.append(if (it.rootAnnotation.name == "xD") "xD" else it.rootAnnotation.name.capitalize())
                builder.append(' ').append(it.rootAnnotation.usage)
                if (it.rootAnnotation.aliases.isNotEmpty()) {
                    builder.append(' ').append(Arrays.toString(it.rootAnnotation.aliases.map { it.capitalize() }.toTypedArray()))
                }
                builder.append(": " + it.rootAnnotation.description)
                builder.append("\n")
            }
            out[type] = builder.append("\n```").toString().trim()
        }

        for (type in CommandType.values()) {
            embed.field(type.name.toLowerCase().capitalize(), out[type] ?: continue, true)
        }
        context(embed) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(45, TimeUnit.SECONDS) } }
    }
}