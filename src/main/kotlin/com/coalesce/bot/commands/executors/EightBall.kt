package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.*
import java.awt.Color
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class EightBall : Embeddables {

    enum class EightBallType(val color: Color) {
        POSITIVE(Color(24, 201, 89)), NEUTRAL(Color(255, 209, 45)), NEGATIVE(Color(237, 45, 35))
    }

    val responses = listOf("It is certain" to EightBallType.POSITIVE, "It is decidedly so" to EightBallType.POSITIVE,
            "Without a doubt" to EightBallType.POSITIVE, "Yes, definitely" to EightBallType.POSITIVE,
            "You may rely on it" to EightBallType.POSITIVE, "As I see it, yes" to EightBallType.POSITIVE,
            "Most likely" to EightBallType.POSITIVE, "Outlook good" to EightBallType.POSITIVE, "Yes" to EightBallType.POSITIVE,
            "Signs point to yes" to EightBallType.POSITIVE,
            "Reply hazy. Try again" to EightBallType.NEUTRAL, "Ask again later" to EightBallType.NEUTRAL,
            "Better not tell you now" to EightBallType.NEUTRAL, "Cannot predict now" to EightBallType.NEUTRAL,
            "Concentrate and ask again" to EightBallType.NEUTRAL,
            "Don't count on it" to EightBallType.NEGATIVE, "My reply is no" to EightBallType.NEGATIVE, "My sources say no" to EightBallType.NEGATIVE,
            "Outlook not so good" to EightBallType.NEGATIVE, "Very doubtful" to EightBallType.NEGATIVE)

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
        val (answer, type) = responses[Random(hash(question)).nextInt(responses.size)]

        context(
                embed().apply {
                    setTitle("8-Ball", null)
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setColor(type.color)
                    addField("Question", question, false)
                    addField("Answer", answer, false)
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