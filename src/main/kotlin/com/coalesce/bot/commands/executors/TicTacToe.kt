package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.subList
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.util.*

class TicTacToe @Inject constructor(val bot: Main): ChatGame("TicTacToe", 5.0) {
    @RootCommand(
            name = "TicTaeToe",
            type = CommandType.FUN,
            permission = "commands.tictaetoe",
            description = "Play a tic tae toe game!",
            aliases = arrayOf("ttt"),
            globalCooldown = 30.0
    )
    fun execute(context: RootCommandContext) = handleCommand(context)

    @JDAListener
    fun react(event: MessageReactionAddEvent, context: EventContext) {
        if (event.user.isBot || bot.listener.isBlacklisted(event.user)) return //Bad boys can't do this
        react(event)
    }

    override fun generateMatch(players: Array<User>, resultHandler: (Map<User, Int>) -> Unit): ChatMatch = TickTacToeMatch(this, players, resultHandler)
}

class TickTacToeMatch(
        chatGame: TicTacToe,
        players: Array<User>,
        resultHandler: (positions: Map<User, Int>) -> Unit
): ChatMatch(chatGame, players, resultHandler) {
    /*
    0 1 2
    3 4 5
    6 7 8
     */
    private val emotes = mutableMapOf<User, String>().apply {
        val allEmotes = arrayOf("❌", "⭕")
        players.forEachIndexed { i, it -> this[it] = allEmotes[i] }
    }
    private val winConditions = mutableListOf<Array<Int>>().apply {
        // Diagonals (Hardcoded because cba)
        add(arrayOf(0, 4, 8))
        add(arrayOf(2, 4, 6))
        /* Vertical */ for (x in 0 .. 2) add(arrayOf(x, x + 3, x + 6))
        /* Horizontal */ arrayOf(0, 3, 6).forEach { add(arrayOf(it, it + 1, it + 2)) }
    }
    private var turnQueue = mutableListOf<User>().apply { addAll(players) }
    private val board = listOf<Piece>()

    override fun messaged(from: User, content: String): Boolean {
        keepAlive()
        if (turnQueue.first() != from) return false
        val numb = content.toIntOrNull() ?: return false
        if (numb > 9) return false

        val piece = board[numb]
        if (piece.emote != null) return true
        piece.emote = emotes[from]
        detectVictory()

        turnQueue = turnQueue.subList(1).toMutableList()
        turnQueue.add(from)
        return true
    }

    private fun detectVictory() {
        winConditions.forEach { winCondition ->
            emotes.forEach { entry ->
                if (winCondition.all { board[it].emote == entry.value }) {
                    val map = mutableMapOf<User, Int>()
                    players.forEach { map[it] = if (entry.key == it) 1 else 0 }
                    invoke(map)
                    return@detectVictory
                }
            }
        }
    }

    override fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message) { }

    data class Piece(var emote: String? = null)
}