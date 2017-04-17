package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.punishments.Punishment
import com.coalesce.punishments.PunishmentManager
import com.coalesce.punishments.Reason
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

@Command(name = "Punish", permission = "commands.punish", description = "Punishes users")
class Punish : CommandExecutor() {
    val manager = PunishmentManager()

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if(message.mentionedUsers.size != 0){
            channel.sendMessage("You need to mention an user to use this command.").queue()
            return
        }
        if(args.size < 2){
            channel.sendMessage("Invalid usage, usage is: !punish <mention> <reason> [description]").queue()
            return
        }

        val user = message.mentionedUsers[0]
        var reason : Reason
        try {
            reason = Reason.valueOf(args[1])
        } catch (e: Exception) {
            val errorMessages = StringBuilder()
            Reason.values().forEach { errorMessages.append(it.toString() + " (" + it.description + ") ") }

            channel.sendMessage("That reason does not exist. Here's a list of valid reasons:\n" + errorMessages.toString()).queue()
            return
        }

        var description : String? = null
        if(args.size > 2) description = args[2]

        val history = manager.findPunishments(message.author)
        val punishment = Punishment(reason, message.author, message.author.id, description)

        val newHistory = punishment.doActUpon(history, user, channel)
        manager.saveChanges(user, newHistory)
    }
}