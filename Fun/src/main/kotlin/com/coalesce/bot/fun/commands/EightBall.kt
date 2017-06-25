package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.VarArg
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embTitle
import java.awt.Color
import java.util.concurrent.ThreadLocalRandom

@Command("eightball", "8ball ball answerme")
class EightBall: Embeddables {
    private val pos = Color(112, 255, 45)
    private val neu = Color(255, 227, 17)
    private val neg = Color(206, 28, 28)
    val responses = mapOf("It is certain" to pos, "It is decidedly so" to pos, "Without a doubt" to pos, "Yes, definitely" to pos,
            "You may rely on it" to pos, "As I see it, yes" to pos, "Most likely" to pos, "Outlook good" to pos, "Yes" to pos,
            "Signs point to yes" to pos,
            "Reply hazy. Try again" to neu, "Ask again later" to neu, "Better not tell you now" to neu, "Cannot predict now" to neu,
            "Concentrate and ask again" to neu, "Don't count on it" to neu, "My reply is no" to neg, "My sources say no" to neg,
            "Outlook not so good" to neg, "Very doubtful" to neg)

    @CommandAlias("Let the magic ball answer")
    fun eightBall(context: CommandContext, @VarArg question: String) {
        context(embed().apply {
            val answer = responses.entries.toList()[ThreadLocalRandom.current().nextInt(responses.size)]

            embTitle = "8ball"
            embColor = answer.value
            field("Question", question, false)
            field("Answer", answer.key, false)
        })
    }
}