package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "hillary", aliases = arrayOf("ctrlalthillary", "ctrlaltdel", "cad", "cah"), description = "Posts a Ctrl Alt Hillary meme into chat.", permission = "commands.hillary")
class Hillary : CommandExecutor() {
    val timeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS)
    var lastUsed : Long = 0

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if(System.currentTimeMillis() - lastUsed > timeout){
            lastUsed = System.currentTimeMillis()
            throw CommandError("https://img.ifcdn.com/images/52044d8cf149969d9c481f9c3cbaff58c888477271180ebff107fd1d1b974a3f_1.jpg")
        }else throw CommandError("This command is in cooldown for " + TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastUsed, TimeUnit.MILLISECONDS) + " seconds.")
    }
}
