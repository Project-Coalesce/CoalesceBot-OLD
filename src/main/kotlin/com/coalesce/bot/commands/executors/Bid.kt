package com.coalesce.bot.commands.executors

import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.commands.ArgsException
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.respectsLeaderboardsFile
import com.coalesce.bot.utilities.order
import com.coalesce.bot.utilities.parseDouble
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class Bid {
    private val emotes = listOf(
        "💧", "🍄", "🍀", "🍎", "🍪", "❄", "🚀", "🌕", "☀"
    )

    @RootCommand(
            name = "bid",
            type = CommandType.FUN,
            permission = "commands.bid",
            aliases = arrayOf("cassino"),
            description = "Bid respects to try and get more respects!"//,
            /*globalCooldown = 0.0,
            userCooldown = 720.0*/
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            throw ArgsException("Usage: !bid <amount>")
        }
        val serializer = RespectsLeaderboardSerializer(respectsLeaderboardsFile)
        val map = serializer.read()
        val respects = map[context.author.id] ?: 0.0
        val bid = context.args[0].parseDouble() ?: run {
            throw ArgsException("Invalid respect amount inserted.")
        }
        if (bid !in 1..30) throw ArgsException("Only bids between 1 and 30 respects are allowed.")
        if (respects < bid) {
            throw ArgsException("Cannot afford to bid $bid.")
        }

        val emoteSize = emotes.size.toDouble()
        val rng = ThreadLocalRandom.current()
        val validEmotes = emotes.order { o1, o2 -> rng.nextInt() }.subList(0, Math.min(emoteSize / 3 + (bid / 15.0) * (emoteSize - emoteSize / 3), emoteSize).toInt())
        val size = validEmotes.size

        context.send("${context.author.asMention}: You bid **${bid.toInt()} respects**.\nGet ready...") {
            var rows = mutableListOf<MutableList<String>>()
            var animationFramesLeft = 4

            fun generateMessage(handler: () -> Unit) {
                editMessage(StringBuilder().apply {
                    append("${context.author.asMention}: You bid **${bid.toInt()} respects**.\n")
                    rows.forEachIndexed { y, it ->
                        append("${if (y == 1) ">" else "  "} ${it.joinToString(separator = " ")}\n")
                    }
                }.toString()).queueAfter(500L, TimeUnit.MILLISECONDS) { handler() }
            }

            fun animate() {
                animationFramesLeft --

                rows = rows.subList(1, 3).toMutableList()
                rows.add(mutableListOf<String>().apply {
                    for (x in 0..2) {
                        add(validEmotes[rng.nextInt(size)])
                    }
                })

                if (animationFramesLeft == 0) {
                    // Finish, change respects, etc
                    editMessage(StringBuilder().apply {
                        val middleRowCount = mutableMapOf<String, Int>()
                        append("${context.author.asMention}: You bid **${bid.toInt()} respects**.\n")
                        rows.forEachIndexed { y, it ->
                            append("${if (y == 1) ">" else "  "} ${it.joinToString(separator = " ")}\n")
                            if (y == 1) it.forEach { middleRowCount[it] = (middleRowCount[it] ?: 0) + 1 }
                        }

                        val (amount, appendMessage) = findMatches(middleRowCount, bid)
                        append(appendMessage + " ($respects -> ${respects + amount})")

                        setRespects(context.author, context.channel, { it + amount }, serializer, map)
                    }.toString()).queue()
                } else {
                    generateMessage(::animate)
                }
            }

            for (y in 0..2) {
                val curList = mutableListOf<String>()
                for (x in 0..2) {
                    val emote = validEmotes[rng.nextInt(size)]
                    curList.add(emote)
                }
                rows.add(curList)
            }
            generateMessage(::animate)
        }
    }

    private fun findMatches(middleRow: Map<String, Int>, bid: Double): Pair<Double, String> =
        if (middleRow.any { it.value == 2 }) bid / 2 to "**Match!** +${bid / 2} respects"
        else if (middleRow.any { it.value == 3 && it.key == "🍀" }) bid * 4 to "🍀🍀 **JACKPOT!** 🍀🍀  +${bid * 4} respects"
        else if (middleRow.all { it.value == 3 }) bid * 1.25 to "**Lucky!** +${bid * 1.25} respects"
        else -bid to "Nothing! -$bid respects"
}