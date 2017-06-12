package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.Embeddables
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import java.awt.Color
import java.util.concurrent.ThreadLocalRandom

class EightBall : Embeddables {

    val responses = arrayOf("It is certain", "It is decidedly so", "Without a doubt", "Yes, definitely",
            "You may rely on it", "As I see it, yes", "Most likely", "Outlook good", "Yes", "Signs point to yes",
            "Reply hazy try again", "Ask again later", "Better not tell you now", "Cannot predict now",
            "Concentrate and ask again", "Don't count on it", "My reply is no", "My sources say no",
            "Outlook not so good", "Very doubtful")

    @RootCommand(
            name = "eightball",
            permission = "commands.eightball",
            type = CommandType.FUN,
            aliases = arrayOf("8ball"),
            usage = "(question)",
            description = "Roll your luck"
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context(context.author, "You have to ask something!")
            return
        }

        context(
                embed().apply {
                    setTitle("8-Ball", null)
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setColor(Color.BLACK)
                    addField("Question", context.args.joinToString(separator = " "), false)
                    addField("Answer", responses[ThreadLocalRandom.current().nextInt(responses.size)], false)
                }
        )
    }
}