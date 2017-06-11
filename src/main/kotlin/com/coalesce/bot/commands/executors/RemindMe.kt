package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.concurrent.TimeUnit

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

        val timeUnit = try {
            TimeUnit.valueOf(context.args[1].toUpperCase())
        } catch (ex: IllegalArgumentException) {
            mention("The time unit given is invalid!"); return
        }

        val message = context.args.copyOfRange(2, context.args.size).joinToString(separator = " ")

        if (time < 0) {
            mention("Time must be greater than zero!")
            return
        }

        if (!context.author.hasPrivateChannel()) {
            mention("You must enable private messages!")
            return
        }

        context(EmbedBuilder().apply {
            setTitle("Reminder", null)
            setAuthor(context.author.name, null, context.author.avatarUrl)
            setColor(Color.GREEN)
            setDescription("I'll be reminding you in $time ${timeUnit.toString().toLowerCase()}")
        })
        context.author.privateChannel.sendMessage(
                EmbedBuilder().apply {
                    setTitle("Reminder from $time ${timeUnit.toString().toLowerCase()} ago", null)
                    setDescription(message)
                }.build()
        ).queueAfter(time.toLong(), timeUnit)
    }
}