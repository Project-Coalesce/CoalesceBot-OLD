package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "Rules", aliases = {"Rulez"}, permission = "commands.rules", description = "Hey you... Yes, you! Want to see the rules?")
class Pi : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {                    
          event.channel.sendMessage(MessageBuilder().append(event.message.author).appendFormat(": Ptssss, you... Head over to %s\n%s", event.guild.getTextChannelById(269178364483338250L).asMention,
                 "http://m.imgur.com/B50EQKp?r").build()).queue { it.delete().queueAfter(10, TimeUnit.SECONDS) }
    }
}
