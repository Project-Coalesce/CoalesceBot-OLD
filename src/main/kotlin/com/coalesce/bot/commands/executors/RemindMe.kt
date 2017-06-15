package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.parseTimeUnit
import net.dv8tion.jda.core.entities.PrivateChannel

class RemindMe {

    @RootCommand(
            name = "RemindMe",
            permission = "commands.remindme",
            type = CommandType.INFORMATION,
            usage = "<time> <unit> (message)",
            description = "Need to remind something?",
            globalCooldown = 10.0
    )
    fun execute(context: RootCommandContext) {
        fun mention(text: String) {
            context(context.author, text)
        }

        if (context.args.isEmpty() || context.args.size < 3) {
            mention("Usage: `!remindme <time> <unit> (message)`")
            return
        }

        val time = context.args[0].toIntOrNull() ?: run { mention("The time must be a number!"); return }
        val timeUnit = context.args[1].parseTimeUnit() ?: run { mention("Invalid unit!"); return }

        val message = context.args.copyOfRange(2, context.args.size).joinToString(separator = " ")

        if (time < 0) {
            mention("Time must be greater than zero!")
            return
        }

        var privateChannel: PrivateChannel
        if (!context.author.hasPrivateChannel()) privateChannel = context.author.openPrivateChannel().complete()
        else privateChannel = context.author.privateChannel

        context(context.author, "I'll be reminding you in $time ${timeUnit.toString().toLowerCase()}")
        privateChannel.sendMessage("${context.author.asMention}: Reminder from $time ${timeUnit.toString().toLowerCase()} ago: \"$message\"", null)
                .queueAfter(time.toLong(), timeUnit)
    }
}