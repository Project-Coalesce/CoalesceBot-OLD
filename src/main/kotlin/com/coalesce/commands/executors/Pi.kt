package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.util.concurrent.TimeUnit

@Command(name = "Pi", permission = "commands.pi", description = "Shows pi.")
class Pi : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        message.channel.sendMessage(EmbedBuilder().setColor(Color(0.0f, 0.5f, 0.0f)).addField("Receiver", message.author.asMention, true).addField("Value of Pi", Math.PI.toString(), true).build()).queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
    }
}