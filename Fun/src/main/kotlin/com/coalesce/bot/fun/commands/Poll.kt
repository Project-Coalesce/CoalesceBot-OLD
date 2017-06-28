package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.*
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Command("Poll")
class Poll: Embeddables {
    /*
     * TARGETED TO USER POLLS, NOT ADMINISTRATION ONES
     */
    @CommandAlias("Create a quick poll")
    fun execute(context: CommandContext, time: Calendar, @VarArg message: String) {
        if (System.currentTimeMillis() <= time.timeInMillis) throw ArgsException("Invalid time.")
        val split = message.split("|")
        if (split.size < 3) throw ArgsException("Invalid syntax!")

        val title = split[0]
        val answers = split.subList(1)

        context(embed().apply {
            embTitle = "$title (User created poll)"
            embColor = Color.YELLOW
            description {
                answers.forEachIndexed { index, s -> appendln("**$index:** $s") }
                append("Click on the respective reaction!")
            }
            setFooter("Voting ends on: " + SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(time.time), null)
        }) {
            for (i in 0.. answers.size -1) {
                addReaction("${'\u0030' + i}\u20E3").queue()
            }

            timeOutHandler(System.currentTimeMillis() - time.timeInMillis, TimeUnit.MILLISECONDS) {
                context.channel.sendTyping().queue()
                editMessage(EmbedBuilder(embeds.first()).apply {
                    embDescription = "Voting has ended!"
                }.build()).queue()
                clearReactions().queue()
                context(embed().apply {
                    embTitle = "Voting results (Poll: $title)"
                    embColor = Color.YELLOW

                    var votes = 0
                    val winner = mutableListOf<Int>()
                    description {
                        reactions.forEach { reaction ->
                            val value = reaction.emote.name[0] - '\u0030'
                            if (value !in 0 .. answers.size - 1) return@forEach

                            answers[value].let {
                                append("${reaction.emote.name} **$it** ${reaction.count - 1} votes\n")

                                if (reaction.count - 1 > votes) {
                                    winner.clear()
                                    votes = reaction.count - 1
                                    winner += value
                                } else if (reaction.count - 1 == votes) {
                                    winner += value
                                }
                            }
                        }
                    }
                    field("Winner", winner.joinToString(separator = ", ") { answers[it] })
                })
            }
        }
    }
}