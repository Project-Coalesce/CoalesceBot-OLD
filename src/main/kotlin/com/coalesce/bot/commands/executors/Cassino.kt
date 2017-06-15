package com.coalesce.bot.commands.executors

import com.coalesce.bot.binary.RespectsLeaderboardSerializer
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
            context("* Usage: !bid <respects>")
            return
        }
        val serializer = RespectsLeaderboardSerializer(respectsLeaderboardsFile)
        val map = serializer.read()
        val respects = map[context.author.id] ?: 0.0
        val bid = context.args[0].parseDouble() ?: run {
            context("* Invalid respects inserted.")
            return@execute
        }
        if (bid > 15) {
            context("* Only bids up to **15 respect** are allowed.")
            return
        }
        if (respects < bid) {
            context("* You cannot afford to bid that much.")
            return
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

        val message = StringBuilder("${context.author.asMention}: You bet ${bid.toInt()}.\n")
        val appendMessage: String
        val amount: Double

        if (middleRowCount.any { it.value == 2 }) {
            amount = bid / 2
            appendMessage = "**Match!** +50% of bid"
        } else if (middleRowCount.all { it.value == 3 }) {
            amount = bid * 1.25
            appendMessage = "🍀 **Lucky!** 🍀 +125% of bid"
        } else {
            amount = -bid
            appendMessage = "Nothing! -100% of bid"
        }

        message.append(appendMessage + " ($respects -> ${respects + amount})\n")
        board.forEachIndexed { y, it ->
            message.append("${if (y == 1) ">" else "  "} ${it.joinToString(separator = " ")}\n")
        }

        setRespects(context.author, context.channel, { it + amount }, serializer, map)
        context(message.toString())
    }
}