package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.subList
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.io.IOException

private val allEmotes = arrayOf("❌", "⭕", "❗", "❓")
private val playerCountForSize = mapOf(
        2 to 3,
        3 to 4,
        4 to 6
)

class TicTacToe @Inject constructor(val bot: Main) {
    private val game = TicTacToeGame()

    @RootCommand(
            name = "TicTacToe",
            type = CommandType.FUN,
            permission = "commands.tictactoe",
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

class TicTacToeGame: ChatGame("TicTacToe", 5.0, allEmotes.size - 1) {
    override fun generateMatch(channel: MessageChannel, players: Array<User>, resultHandler: (Map<User, Int>) -> Unit): ChatMatch = TicTacToeMatch(channel, this, players, resultHandler)
}

class TicTacToeMatch(
        channel: MessageChannel,
        chatGame: TicTacToeGame,
        players: Array<User>,
        resultHandler: (positions: Map<User, Int>) -> Unit
): ChatMatch(channel, chatGame, players, resultHandler) {
    private val size = (playerCountForSize[players.size] ?: throw IOException("Illegal amount of players!"))
    private val indexTransformAmount = arrayOf(1, size, size + 1, size - 1)
    private val matchers = arrayOf<(Int, Int) -> Array<Int>>(
            { a, b -> arrayOf(a + b, a - b) },
            { a, b -> arrayOf(a + b, a + b * 2) },
            { a, b -> arrayOf(a - b, a - b * 2) }
    )
    private val tileCount = size * size
    private val emotes = mutableMapOf<User, String>().apply { players.forEachIndexed { i, it -> this[it] = allEmotes[i] } }
    private var turnQueue = mutableListOf<User>().apply { addAll(players) }
    private val board = mutableListOf<Piece>().apply { for (i in 0..tileCount - 1) add(Piece(null)) }

    init {
        print(turnQueue[0])
    }

    private fun detectVictory(insertedIndex: Int, user: User, piece: String): Boolean {
        indexTransformAmount.forEach { numb ->
            if (matchers.any { it(insertedIndex, numb).all { it in 0..tileCount - 1 && board[it].emote == piece } }) {
                // Victory to user!
                val map = mutableMapOf<User, Int>()
                players.forEach { map[it] = if (user == it) 1 else 0 }
                invoke(map)
                return@detectVictory true
            }
        }

        if (board.all { it.emote != null }) {
            val map = mutableMapOf<User, Int>()
            players.forEach { map[it] = 0 }
            invoke(map)
            return true
        }
        return false
    }

    override fun messaged(from: User, content: String): Boolean {
        keepAlive()
        if (turnQueue.first() != from) return false
        val numb = content.toIntOrNull() ?: return false
        if (numb > tileCount - 1) return false

        val piece = board[numb]
        if (piece.emote != null) return true
        val emote = emotes[from]!!
        piece.emote = emote

        if (detectVictory(numb, from, emote)) {
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
            append("```")
            board.forEachIndexed { index, piece ->
                if (index % 3 == 0) append("\n")
                val character = (piece.emote ?: index.toString())
                append((if (character == "⭕" || character == "❓" || character == "❌") " " else "  ") + character)
            }
            append("\n```")
            if (turn != null) append("\n**${turn.asMention}'s turn!**")
        }.toString()).queue()
    }

    override fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message) { }

    data class Piece(var emote: String? = null)
}