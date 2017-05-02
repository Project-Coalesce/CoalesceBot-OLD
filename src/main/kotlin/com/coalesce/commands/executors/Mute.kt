package com.coalesce.commands.executors

import com.coalesce.Bot
import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import com.coalesce.punishmentals.Punishment
import com.coalesce.punishmentals.PunishmentManager
import com.coalesce.punishmentals.Reason
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.*

@Command(name = "Mute", permission = "commands.mute", description = "Allows for muting a user permanently or for a specific time", type = CommandType.ADMINISTRATION)
class Mute : CommandExecutor() {
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
            channel.sendMessage("Invalid usage, proper usage is: !mute <mention> <time> [description]").queue()
            return
        }
        val user = message.mentionedUsers[0]

        var description: String? = null
        if (args.size > 2) {
            val desc = StringBuilder()
            Arrays.asList(args).subList(2, args.size).forEach { desc.append(it + " ") }
            description = desc.toString()
        }

        var time = Calendar.getInstance()
        if (args[1].equals("permanent", true) || args[1].equals("null", true) || args[1].equals("-1", false)) {
            time = null
        } else {
            val arg = args[1]
            var timeUnit: Int = Calendar.DAY_OF_MONTH
            val timeAdd = Integer.parseInt(arg.substring(0, arg.length - 1))

            if (arg.endsWith("h")) timeUnit = Calendar.HOUR
            else if (arg.endsWith("m")) timeUnit = Calendar.MINUTE
            else if (arg.endsWith("s")) timeUnit = Calendar.SECOND
            else if (arg.endsWith("w")) timeUnit = Calendar.WEEK_OF_MONTH
            else if (arg.endsWith("M")) timeUnit = Calendar.MONTH

            time.add(timeUnit, timeAdd)
        }

        val punishment = Punishment(Reason.GENERAL_WARNING, user, message.author, description, time.timeInMillis)
        punishment.doActUpon(PunishmentManager.instance[user], channel)
        PunishmentManager.instance[user] = punishment
    }
}