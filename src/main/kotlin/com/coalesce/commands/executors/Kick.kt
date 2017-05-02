package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.*

@Command(name = "Kick", permission = "commands.kick", description = "Allows for kicking a user", type = CommandType.ADMINISTRATION)
class Kick : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (message.guild.id == "268187052753944576" /* Debug Purposes */ &&
                !message.guild.getMember(message.author).roles.contains(Bot.instance.jda.getRoleById("268239031467376640"))) {
            channel.sendMessage("You lack permission to use this command.").queue()
            return
        }

        if (message.mentionedUsers.size != 1) {
            channel.sendMessage("You need to mention a user to use this command.").queue()
            return
        }

        if (args.isEmpty()) {
            channel.sendMessage("Invalid usage, proper usage is: !kick <mention> [description]").queue()
            return
        }
        val user = message.mentionedUsers[0]

        var description: String? = null
        if (args.size > 1) {
            val desc = StringBuilder()
            Arrays.asList(args).subList(1, args.size).forEach { desc.append(it + " ") }
            description = desc.toString()
        }
        //TODO: Use description somehow, maybe private message the user?

        val guild = Bot.instance.jda.getGuildById("268187052753944576")
        val member = guild.getMember(user)

        guild.controller.kick(member)
    }
}
