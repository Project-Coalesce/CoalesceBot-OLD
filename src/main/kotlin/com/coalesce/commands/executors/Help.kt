package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandEntry
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.util.*

@Command(name = "help", description = "Lists the commands", aliases = arrayOf("?", "h"), permission = "commands.help")
class Help : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        val mb = MessageBuilder()
        mb.append("```\n")

        val entries = ArrayList<CommandEntry>()
        commandMap.entries.values.forEach { e ->
            if (!entries.contains(e)) {
                entries.add(e)
                mb.append(if (e.annotation.name.equals("xD")) "xD" else e.annotation.name.capitalize()).append(' ').append(e.annotation.usage).append(' ')
                        .append(if (e.annotation.aliases.isEmpty())
                            ""
                        else
                            Arrays.toString(e.annotation.aliases.flatMapTo(mutableListOf<String>(), { setOf(it.capitalize()) }).toTypedArray()))
                        .append("\n")
            }
        }
        entries.clear()

        mb.append("```")

        channel.sendMessage(EmbedBuilder()
                .setTitle("Help", null)
                .setColor(Color.GREEN)
                .setDescription(mb.build().rawContent)
                .build()).queue()
    }
}
