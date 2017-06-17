package com.coalesce.bot.commands.executors

import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.commands.ArgsException
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.respectsLeaderboardsFile
import com.coalesce.bot.utilities.parseDouble
import java.util.concurrent.ThreadLocalRandom

class Cassino {
    private val emotes = listOf(
        "💧", "🍄", "🍀", "🍎", "🍪", "❄", "🚀", "🌕", "☀"
    )

    @RootCommand(
            name = "bid",
            type = CommandType.FUN,
            permission = "commands.bid",
            aliases = arrayOf("cassino"),
            description = "Bid respects to try and get more respects!",
            globalCooldown = 0.0,
            userCooldown = 720.0
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
        if (bid !in 0..30) throw ArgsException("Only bids between 0 and 15 respects are allowed.")
        if (respects < bid) {
            throw ArgsException("Cannot afford to bid $bid.")
        }

        val emoteSize = emotes.size.toDouble()
        val validEmotes = emotes.subList(0, Math.min(emoteSize / 3 + (bid / 15.0) * (emoteSize - emoteSize / 3), emoteSize).toInt())
        val rng = ThreadLocalRandom.current()
        val size = validEmotes.size
        val middleRowCount = mutableMapOf<String, Int>()

        val board = mutableListOf<List<String>>()
        for (y in 0..2) {
            val curList = mutableListOf<String>()
            for (x in 0..2) {
                val emote = validEmotes[rng.nextInt(size)]
                curList.add(emote)
                if (y == 1) {
                    middleRowCount[emote] = (middleRowCount[emote] ?: 0) + 1
                }
            }
            board.add(curList)
        }

        val message = StringBuilder("${context.author.asMention}: You bid **${bid.toInt()} respects**.\n")
        val (amount, appendMessage) = findMatches(middleRowCount, bid)

        message.append(appendMessage + " ($respects -> ${respects + amount})\n")
        board.forEachIndexed { y, it ->
            message.append("${if (y == 1) ">" else "  "} ${it.joinToString(separator = " ")}\n")
        }

        setRespects(context.author, context.channel, { it + amount }, serializer, map)
        context(message.toString())
    }

    private fun findMatches(middleRow: Map<String, Int>, bid: Double): Pair<Double, String> =
        if (middleRow.any { it.value == 2 }) bid / 2 to "**Match!** +50% of bid"
        else if (middleRow.all { it.value == 3 }) bid * 1.25 to "🍀 **Lucky!** 🍀 +125% of bid"
        else -bid to "Nothing! -100% of bid"
}