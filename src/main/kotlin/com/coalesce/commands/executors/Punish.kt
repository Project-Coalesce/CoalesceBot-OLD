package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import com.coalesce.punishments.Punishment
import com.coalesce.punishments.Reason
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.*

@Command(name = "Punish", permission = "commands.punish", description = "Automatically creates a punishment and server logs it",
        type = CommandType.ADMINISTRATION)
class Punish : CommandExecutor() {
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

        if (args.size < 2) {
            channel.sendMessage("Invalid usage, proper usage is: !punish <mention> <reason> [description]").queue()
            return
        }

        val user = message.mentionedUsers[0]
        var reason: Reason
        try {
            reason = Reason.valueOf(args[1])
        } catch (e: Exception) {
            val errorMessages = StringBuilder()
            Reason.values().forEach { errorMessages.append(it.toString() + " (" + it.description + " | Severity " + it.severity + ") ") }

            channel.sendMessage("That reason does not exist. Here's a list of valid reasons:\n" + errorMessages.toString()).queue()
            return
        }

        var description: String? = null
        if (args.size > 2) {
            val desc = StringBuilder()
            Arrays.asList(args).subList(2, args.size).forEach { desc.append(it + " ") }
            description = desc.toString()
        }

        val history = Bot.instance.manager.findPunishments(user)
        val punishment = Punishment(reason, message.author, message.author.id, description)

        val newHistory = punishment.doActUpon(history, user, channel)
        Bot.instance.manager.saveChanges(user, newHistory)
    }
}