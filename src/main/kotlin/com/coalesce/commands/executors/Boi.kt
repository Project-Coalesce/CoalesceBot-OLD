package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

@Command(name = "boi", aliases = arrayOf("njsblessing"), description = "Posts a boi meme into chat.", permission = "commands.boi")
class Boi : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        throw CommandError(
                if (message.author.name.matches("[0-9a-zA-Z]+".toRegex()))
                    "http://i1.kym-cdn.com/photos/images/newsfeed/001/183/604/ee9.png"
                else
                    "http://i.imgur.com/hOYMcij.jpg"
        )
    }
}