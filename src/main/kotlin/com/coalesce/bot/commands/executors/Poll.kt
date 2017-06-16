package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.parseTimeUnit
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class Poll : Embeddables {
    /**
     * TARGETED TO USER POLLS, NOT ADMINISTRATION ONES
     */

    @RootCommand(
            name = "Poll",
            permission = "commands.poll",
            type = CommandType.INFORMATION,
            description = "Create a poll",
            usage = "<name> <time> <unit> (option)|(option)|(etc)",
            globalCooldown = 30.0
    )
    fun execute(context: RootCommandContext) {
        fun mention(text: String) {
            context(context.author, text)
        }

        if (context.args.isEmpty() || context.args.size < 3) {
            throw ArgsException("Usage: `!poll <name> <time> <unit> (option)|(option)|(etc)`")
        }

        val name = context.args[0]

        val time = context.args[1].toIntOrNull() ?: run { context("* Time must be a number!"); return }
        val timeUnit = context.args[2].parseTimeUnit() ?: run { context("* Invalid unit!"); return }

        val options = context.args.copyOfRange(3, context.args.size).joinToString(separator = " ").split("|").map(String::trim)
        if (options.size in 0..10) {
            throw ArgsException("The size of options must be greater than 1, and shouldn't exceed 10!")
        }

        val channel = context.channel
        channel.sendMessage(
                embed().apply {
                    setColor(Color.YELLOW)
                    setAuthor(context.author.name, null, context.author.avatarUrl)
                    setTitle("Poll: $name", null)
                    setDescription("Vote by clicking the reactions!")

                    val builder = StringBuilder()
                    options.forEachIndexed { index, string ->
                        builder.append("**$index.** $string").append("\n")
                    }
                    addField("Options", builder.toString(), true)
                    setFooter("Voting will end in $time ${timeUnit.toString().toLowerCase()}", null)
                }.build()
        ).queue {
            channel.sendTyping()
            for (i in 0.. options.size -1) {
                it.addReaction("${'\u0030' + i}\u20E3").queue()
            }

            it.editMessage(EmbedBuilder(it.embeds[0]).apply {
                setDescription("Voting has ended!")
                clearFields()
            }.build()).queueAfter(time.toLong(), timeUnit) {
                channel.sendMessage(
                        EmbedBuilder().apply {
                            setColor(Color.YELLOW)
                            setAuthor(context.author.name, null, context.author.avatarUrl)
                            setTitle("Results for: $name", null)

                            var votes = 0
                            val winner = mutableListOf<Int>()

                            setDescription(StringBuilder().apply {
                                it.reactions.forEach { reaction ->
                                    val value = reaction.emote.name[0] - '\u0030'
                                    if (value !in 0 .. options.size - 1) return@forEach

                                    options[value].let {
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
                            }.toString())

                            it.clearReactions().queue()
                            addField("Winner", winner.joinToString(prefix = "**", postfix = "**") {options[it]}, true)
                        }.build()
                ).queue()
            }
        }
    }
}