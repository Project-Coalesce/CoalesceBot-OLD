package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

@Command(name = "enough", aliases = arrayOf("proxisreaction"), description = "I think you've had enough!", permission = "commands.enough")
class Enough : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        throw CommandError(
                    "http://i1.kym-cdn.com/photos/images/newsfeed/000/917/654/8a4.jpg"
        )
    }
}
