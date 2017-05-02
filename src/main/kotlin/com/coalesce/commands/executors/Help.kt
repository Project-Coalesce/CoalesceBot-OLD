package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.util.*
import java.util.concurrent.TimeUnit

@Command(name = "help", description = "Lists the commands", aliases = arrayOf("?", "h"), permission = "commands.help",
        globalCooldown = 35, type = CommandType.INFORMATION)
class Help : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        val embedBuilder = EmbedBuilder().setTitle("Help", null).setColor(Color.GREEN)

        val map = mutableMapOf<CommandType, MutableSet<CommandExecutor>>().withDefault { mutableSetOf() }
        commandMap.entries.values.forEach {
            map[it.annotation.type] = map[it.annotation.type]?.apply { add(it.executor) } ?: mutableSetOf()
        }
        val out = mutableMapOf<CommandType, String>().withDefault { "None" }
        map.forEach { type, executors ->
            val builder = StringBuilder().append("```\n")
            executors.forEach {
                if (it.annotation.name.equals("xD", false)) {
                    builder.append("xD")
                } else {
                    builder.append(it.annotation.name.capitalize())
                }
                builder.append(' ').append(it.annotation.usage)
                if (!it.annotation.aliases.isEmpty()) {
                    builder.append(' ').append(Arrays.toString(it.annotation.aliases.flatMapTo(mutableSetOf<String>(), { setOf(it.capitalize()) }).toTypedArray()))
                }
                builder.append("\n")
            }
            out[type] = builder.append("```").toString().trim()
        }

        embedBuilder.addField("Fun", out[CommandType.FUN], true)
                .addField("Information", out[CommandType.INFORMATION], true)
                .addField("Administration", out[CommandType.ADMINISTRATION], true)
                .addField("Debug", out[CommandType.DEBUG], true)

        channel.sendMessage(embedBuilder.build()).queue { it.delete().queueAfter(25, TimeUnit.SECONDS) }
    }
}
