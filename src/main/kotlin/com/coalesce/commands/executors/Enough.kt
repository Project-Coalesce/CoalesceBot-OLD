package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

@Command(name = "enough", aliases = arrayOf("proxisreaction"), description = "Meme Command (I think you've had enough!)", permission = "commands.enough",
        cooldown = 10,type = CommandType.FUN)
class Enough : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        throw CommandError("http://i1.kym-cdn.com/photos/images/newsfeed/000/917/654/8a4.jpg")
    }
}
