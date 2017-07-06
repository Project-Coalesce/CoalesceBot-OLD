package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.*
import net.dv8tion.jda.core.EmbedBuilder

@Command("Poll")
class Poll: Embeddables {
    /*
     * TARGETED TO USER POLLS, NOT ADMINISTRATION ONES
     */
    @Usage("!poll <Time> <Title>|<Answers separated in |>")
    @CommandAlias("Create a quick poll")
    fun execute(context: CommandContext, time: java.util.Calendar, @VarArg message: String) {
        if (time.timeInMillis <= System.currentTimeMillis()) throw ArgsException("Invalid time.")
        val split = message.split("|")
        if (split.size < 3) throw ArgsException("Invalid syntax!")

        val title = split[0]
        val answers = split.subList(1)

        context(embed().apply {
            embTitle = "$title (User created poll)"
            embColor = java.awt.Color.YELLOW
            description {
                answers.forEachIndexed { index, s -> appendln("**$index:** $s") }
                append("Click on the respective reaction!")
            }
            setFooter("Voting ends on: " + java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(time.time), null)
        }) {
            for (i in 0.. answers.size -1) {
                addReaction("${'\u0030' + i}\u20E3").queue()
            }

            timeOutHandler(time.timeInMillis - System.currentTimeMillis(), java.util.concurrent.TimeUnit.MILLISECONDS) {
                context.channel.sendTyping().queue()
                editEmbed {
                    embDescription = "Voting has ended!"
                }
                context(embed().apply {
                    embTitle = "Voting results (Poll: $title)"
                    embColor = java.awt.Color.YELLOW

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
                        clearReactions().queue()
                    }
                    field("Winner(s)", winner.joinToString(separator = ", ") { answers[it] })
                })
            }
        }
    }
}