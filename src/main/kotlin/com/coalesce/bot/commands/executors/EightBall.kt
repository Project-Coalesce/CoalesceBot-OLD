package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.*
import java.awt.Color
import java.util.*
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
            throw ArgsException("You have to ask something!")
        }

        val question = context.args.joinToString(separator = " ")

        context(
                embed().apply {
                    setTitle("8-Ball", null)
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setColor(Color(0x5ea81e))
                    addField("Question", question, false)
                    addField("Answer", responses[Random(hash(question)).nextInt(responses.size)], false)
                }
        )
    }

    // Thanks to https://stackoverflow.com/a/1660613/5572963
    private fun hash(string: String): Long {
        var h = 1125899906842597L // prime
        val len = string.length

        for (i in 0 .. len - 1) {
            h = 31 * h + string[i].toLong()
        }
        return h
    }
}