package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.subList
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent

class TicTacToe @Inject constructor(val bot: Main) {
    private val game = TicTacToeGame()

    @RootCommand(
            name = "TicTacToe",
            type = CommandType.FUN,
            permission = "commands.tictaetoe",
            description = "Play a tic tae toe game!",
            aliases = arrayOf("ttt"),
            globalCooldown = 30.0
    )
    fun execute(context: RootCommandContext) = game.handleCommand(context)

    @JDAListener
    fun react(event: MessageReactionAddEvent, context: EventContext) {
        if (event.user.isBot || bot.listener.isBlacklisted(event.user)) return //Bad boys can't do this
        game.handleReaction(event)
    }

    @JDAListener
    fun message(event: MessageReceivedEvent, context: EventContext) {
        game.handleMessage(event)
    }
}

class TicTacToeGame: ChatGame("TicTacToe", 5.0) {
    override fun generateMatch(channel: MessageChannel, players: Array<User>, resultHandler: (Map<User, Int>) -> Unit): ChatMatch = TicTacToeMatch(channel, this, players, resultHandler)
}

class TicTacToeMatch(
        channel: MessageChannel,
        chatGame: TicTacToeGame,
        players: Array<User>,
        resultHandler: (positions: Map<User, Int>) -> Unit
): ChatMatch(channel, chatGame, players, resultHandler) {
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
    private val board = mutableListOf<Piece>().apply { for (i in 0..8) add(Piece(null)) }

    init {
        print(turnQueue[0])
    }

    override fun messaged(from: User, content: String): Boolean {
        keepAlive()
        if (turnQueue.first() != from) return false
        val numb = content.toIntOrNull() ?: return false
        if (numb > 9) return false

        val piece = board[numb]
        if (piece.emote != null) return true
        piece.emote = emotes[from]

        if (detectVictory()) {
            print(null)
        } else {
            turnQueue = turnQueue.subList(1).toMutableList()
            turnQueue.add(from)
            print(turnQueue[0])
        }
        return true
    }

    private fun print(turn: User?) {
        channel.sendMessage(StringBuilder().apply {
            board.forEachIndexed { index, piece ->
                if (index % 3 == 0) append("\n")
                append(piece.emote ?: "${'\u0030' + index}\u20E3")
            }

            if (turn != null) append("\n**${turn.asMention}'s turn!**")
        }.toString()).queue()
    }

    private fun detectVictory(): Boolean {
        winConditions.forEach { winCondition ->
            emotes.forEach { entry ->
                if (winCondition.all { board[it].emote == entry.value }) {
                    val map = mutableMapOf<User, Int>()
                    players.forEach { map[it] = if (entry.key == it) 1 else 0 }
                    invoke(map)
                    return@detectVictory true
                }
            }
        }
        return false
    }

    override fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message) { }

    data class Piece(var emote: String? = null)
}