package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.ArgsException
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
            throw ArgsException("Usage: `!remindme <time> <unit> (message)`")
        }

        val time = context.args[0].toIntOrNull() ?: run { mention("The time must be a number!"); return }
        val timeUnit = context.args[1].parseTimeUnit() ?: run { mention("Invalid unit!"); return }

        val message = context.args.copyOfRange(2, context.args.size).joinToString(separator = " ")

        if (time < 0) {
            throw ArgsException("Time must be greater than zero!")
        }

        fun reminder(pm: PrivateChannel) {
            pm.sendMessage("${context.author.asMention}: Reminder from $time ${timeUnit.toString().toLowerCase()} ago: \"$message\"", null)
                    .queueAfter(time.toLong(), timeUnit)
        }

        if (!context.author.hasPrivateChannel()) context.author.openPrivateChannel().queue(::reminder)
        else reminder(context.author.privateChannel)

        context(context.author, "I'll be reminding you in $time ${timeUnit.toString().toLowerCase()}")
    }
}