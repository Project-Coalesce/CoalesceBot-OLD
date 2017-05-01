package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

@Command(name = "Rules", aliases = arrayOf("Rulez"), permission = "commands.rules", description = "Shows the rules (Well not really)")
class Rules : CommandExecutor() {
    private var lastUse = -1f

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (lastUse == -1f || lastUse > (System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20))) {
            channel.sendMessage(MessageBuilder().append(message.author).appendFormat(": Ptssss, you... Head over to %s\n%s", "<#269178364483338250>",
                    "http://i.imgur.com/B50EQKp.png").build()).queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
        } else {
            channel.sendMessage("This message is still on a cooldown for another " +
                    "${BigDecimal(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastUse.toLong())).setScale(2, RoundingMode.HALF_EVEN)} seconds.")
                    .queue { it.delete().queueAfter(10, TimeUnit.SECONDS) }
        }
    }
}
