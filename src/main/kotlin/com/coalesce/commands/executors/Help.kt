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
        cooldown = 5, type = CommandType.INFORMATION)
class Help : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {

        val embedBuilder = EmbedBuilder().setTitle("Help", null).setColor(Color.GREEN)

        val map = hashMapOf<CommandType, ArrayList<String>>().withDefault { arrayListOf() }

        commandMap.entries.values.forEach { e ->
            val builder = StringBuilder()

            if (e.annotation.name.equals("xD", false)) builder.append("xD")
            else builder.append(e.annotation.name.capitalize())

            builder.append(' ').append(e.annotation.usage).append(' ')

            if (!e.annotation.aliases.isEmpty()) {
                builder.append(Arrays.toString(e.annotation.aliases.flatMapTo(mutableListOf<String>(), { setOf(it.capitalize()) }).toTypedArray()))
            }

            val list = map.getValue(e.annotation.type)
            list.add(builder.toString())

            map.put(e.annotation.type, list)
        }

        embedBuilder.addField("Fun", map.getValue(CommandType.FUN).joinToString(separator = "\n"), true)
                .addField("Information", map.getValue(CommandType.INFORMATION).joinToString(separator = "\n"), true)
                .addField("Administration", map.getValue(CommandType.ADMINISTRATION).joinToString(separator = "\n"), true)
                .addField("Debug", map.getValue(CommandType.DEBUG).joinToString(separator = "\n"), true)

        channel.sendMessage(embedBuilder.build()).queue { it.delete().queueAfter(25, TimeUnit.SECONDS) }
    }
}
