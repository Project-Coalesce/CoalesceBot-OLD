package com.coalesce.bot.commands.executors

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
        val map = mutableMapOf<CommandType, MutableSet<CommandEntry>>()
        bot.listener.registry.commands.forEach { _, value ->
            if (!value.rootAnnotation.type.hidden) {
                map[value.rootAnnotation.type] = map[value.rootAnnotation.type]?.apply { add(value) } ?: mutableSetOf()
            }
        }

        val out = mutableMapOf<CommandType, String>()
        map.forEach { type, entries ->
            val builder = StringBuilder()
            entries.forEach {
                builder.append("`${if (it.rootAnnotation.name == "xD") "xD" else it.rootAnnotation.name.capitalize()}`")
                builder.append(" ${it.rootAnnotation.usage} ")
                if (it.rootAnnotation.aliases.isNotEmpty()) {
                    builder.append(Arrays.toString(it.rootAnnotation.aliases.map { it.capitalize() }.toTypedArray()))
                }
                builder.append(": ${it.rootAnnotation.description}\n")
            }
            out[type] = builder.toString().trim()
        }

        val messageBuilder = StringBuilder("***Help***\n")
        out.forEach { k, v ->
            messageBuilder.append("\n\n**${k.name.toLowerCase().capitalize()}:**\n$v")
        }
        context(messageBuilder.toString()) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(1, TimeUnit.MINUTES) } }
    }
}