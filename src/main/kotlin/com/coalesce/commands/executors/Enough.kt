package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

@Command(name = "enough", aliases = arrayOf("proxisreaction"), description = "Meme Command (I think you've had enough!)", permission = "commands.enough")
class Enough : CommandExecutor() {
    val timeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS)
    var lastUsed: Long = -1

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (lastUsed == -1.toLong() || (System.currentTimeMillis() + timeout) >= lastUsed) {
            lastUsed = System.currentTimeMillis()
            throw CommandError(
                    "http://i1.kym-cdn.com/photos/images/newsfeed/000/917/654/8a4.jpg"
            )
        } else throw CommandError("This command is in cooldown for ${BigDecimal((Math.abs(lastUsed - System.currentTimeMillis())) * 1000).setScale(2, RoundingMode.HALF_EVEN)} seconds.")
    }
}
