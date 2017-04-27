package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "enough", aliases = arrayOf("proxisreaction"), description = "I think you've had enough!", permission = "commands.enough")
class Enough : CommandExecutor() {
    val timeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS)
    var lastUsed : Long = 0

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if(System.currentTimeMillis() - lastUsed > timeout){
            lastUsed = System.currentTimeMillis()
            throw CommandError(
                    "http://i1.kym-cdn.com/photos/images/newsfeed/000/917/654/8a4.jpg"
            )
        }else throw CommandError("This command is in cooldown for " + TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastUsed, TimeUnit.MILLISECONDS) + " seconds.")
    }
}
