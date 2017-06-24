package com.coalesce.bot.cocoins.commands

import com.coalesce.bot.CoCoinsTransaction
import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.*
import java.awt.Color
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@Command("Slots", "bid bet cassino slot")
@UserCooldown(6L, TimeUnit.MINUTES)
class Slots: Embeddables {
    private val emotes = mutableListOf<String>().apply {
        addAll("💧 🍄 🍀 🍎 🍪 🚀 🌕 ☀ 🎱 🍉 🏆".split(" "))
    }

    @CommandAlias("Bid some ¢$'s in a fun slots game! Trust me its not rigged")
    fun slots(context: CommandContext, amount: Int) {
        if (amount !in 1..15) throw ArgsException("You may only bid between 1 and 15 ¢$'s!")
        val coins = context.main.coCoinsManager
        //if (amount > coins[context.author].total) throw ArgsException("You cannot afford to bid $amount.")
        coins[context.author].transaction(CoCoinsTransaction("Used on slots", -amount.toDouble()), context.channel, context.author)

        val emoteSize = emotes.size
        val rng = ThreadLocalRandom.current()
        val validEmotes = emotes.order { _, _ -> rng.nextInt() }.subList(0, Math.min(emoteSize / 3 + (amount) * (emoteSize - emoteSize / 3), emoteSize))
        val size = validEmotes.size

        val animFrames = 3
        val rows = mutableListOf<MutableList<String>>()
        var animationFramesLeft = animFrames
        var currentColumn = 0

        fun generateMessage() = embed().apply {
            embTitle = "Slots"
            embColor = Color(229, 193, 0)

            description {
                append("||")
                for (i in 0..2) if (currentColumn > i) append("🔒 ") else append("▫ ")
                append("||\n")
                rows.forEachIndexed { y, it ->
                    append("${if (y == 1) ">" else "||"} ${it.joinToString(separator = " ")} ${if (y == 1) "<" else "||"}\n")
                }
            }
        }.build()

        for (y in 0..2) {
            val curList = mutableListOf<String>()
            for (x in 0..2) {
                val emote = validEmotes[rng.nextInt(size)]
                curList.add(emote)
            }
            rows.add(curList)
        }

        context(generateMessage()) {
            fun edit(longer: Boolean, handler: () -> Unit) =
                editMessage(generateMessage()).queueAfter(if (longer) 800L else 400L, TimeUnit.MILLISECONDS) { handler() }

            fun animate() {
                animationFramesLeft --
                val longer = if (animationFramesLeft <= 0) run {
                    animationFramesLeft = animFrames
                    currentColumn ++
                    true
                } else false

                if (currentColumn < 3) {
                    rows.forEachIndexed { row, it ->
                        it.forEachIndexed { column, _ ->
                            if (currentColumn > column) return@forEachIndexed
                            if (row < 2) it[column] = rows[row + 1][column]
                            else it[column] = validEmotes[rng.nextInt(size)]
                        }
                    }

                    edit(longer, ::animate)
                } else {
                    // Finish, change respects, etc
                    val middleRow = mutableMapOf<String, Int>()
                    rows.forEachIndexed { y, it ->
                        if (y == 1) it.forEach { middleRow[it] = (middleRow[it] ?: 0) + 1 }
                    }

                    editMessage(generateMessage()).queue()
                    val (received, message) = findMatches(middleRow, amount.toDouble())
                    coins[context.author].transaction(CoCoinsTransaction(message, amount.toDouble() + received), context.channel, context.author)
                }
            }

            edit(false, ::animate)
        }
    }

    private fun findMatches(middleRow: Map<String, Int>, bid: Double): Pair<Double, String> =
            if (middleRow.any { it.value == 2 }) bid / 2 to "**Match!**"
            else if (middleRow.any { it.value == 3 && it.key == "🍀" }) bid * 4 to "🍀🍀 **JACKPOT!** 🍀🍀"
            else if (middleRow.all { it.value == 3 }) bid * 1.25 to "**Lucky!**"
            else -bid to "Nothing!"
}