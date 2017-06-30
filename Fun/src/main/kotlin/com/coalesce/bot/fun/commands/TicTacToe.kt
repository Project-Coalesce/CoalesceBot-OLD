package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.`fun`.winDetection
import com.coalesce.bot.command.*
import com.coalesce.bot.command.game.CoGame
import com.coalesce.bot.command.game.CoTurnMatch
import com.coalesce.bot.utilities.listOf
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import java.io.IOException

private val characters = listOf("⭕", "❌", "❗", "❓", "🍆", "<:brazil:328369151926075392>")
private val playerCountForSize = mapOf(
        2 to 3,
        3 to 4,
        4 to 5
)

@Command("TicTacToe", "ttt")
class TicTacToe {
    private val game = object: CoGame("TicTacToe", 3, 1, characters.size - 2) {
        override fun match(channel: TextChannel, @VarArg players: List<User>, resultHandler: (Map<User, Int>) -> Unit) =
                TicTacToeMatch(channel, this, players, resultHandler)
    }

    @CommandAlias("Find a tictactoe match")
    fun exec(context: CommandContext, target: List<User> = listOf(), targetAmount: Int = if (target.isEmpty()) 1 else target.size,
                     bid: Int? = null) = game.matchfinding(context, target, targetAmount, bid)

    @JDAListener
    fun messaged(event: MessageReceivedEvent) = game.message(event)

    @JDAListener
    fun reactionAdd(event: GuildMessageReactionAddEvent) = game.react(event)
}

class TicTacToeMatch(
        channel: TextChannel,
        chatGame: CoGame,
        players: List<User>,
        resultHandler: (Map<User, Int>) -> Unit
): CoTurnMatch(channel, chatGame, players, resultHandler) {
    private val boardSize = (playerCountForSize[players.size] ?: throw IOException("Illegal amount of players!"))
    private val tileCount = boardSize * boardSize
    private val emotes = mutableMapOf<User, String>().apply { players.forEachIndexed { i, it -> this[it] = characters[i] } }
    private val board = listOf<String?> { for (i in 0..tileCount - 1) add(null) }
    private var message: Message? = null

    init {
        sendUpdateMessage()
    }

    private fun detectVictory(): Boolean {
        val reorganizedBoard = mutableListOf<MutableList<String?>>()

        for (i in 1..boardSize) {
            reorganizedBoard.add(listOf<String?> {
                var index = i
                while (size < 3) {
                    add(board[index - 1])
                    index += boardSize
                }
            })
        }

        val result = winDetection(reorganizedBoard, boardSize - 1, boardSize - 1, if (boardSize == 3) 3 else 4)
        if (result != null) {
            val winner = emotes.entries.find { it.value == result }!!.key
            val map = mutableMapOf<User, Int>()
            players.forEach { map[it] = if (winner == it) 1 else 0 }
            invoke(map)
            return true
        }
        return false
    }

    override fun messaged(from: User, content: String): Boolean {
        if (!isNext(from)) return false
        val numb = content.toIntOrNull() ?: run {
            if (content.length != 1) return false
            val char = content[0]
            val numbEquiv = char.toInt()
            if (numbEquiv in 65..90) {
                numbEquiv - 65
            } else if (numbEquiv in 97..122) {
                numbEquiv - 97
            } else return false
        }
        if (numb !in 0..tileCount - 1) return false

        val piece = board[numb]
        if (piece != null) return true
        val emote = emotes[from]!!
        board[numb] = emote

        keepAlive()
        if (!detectVictory()) nextTurn()
        sendUpdateMessage()
        return true
    }

    private fun sendUpdateMessage() {
        if (message != null) message!!.delete().queue()
        channel.sendMessage(StringBuilder().apply {
            board.forEachIndexed { index, piece ->
                if (index % boardSize == 0) append("\n")
                append((piece ?: "\uD83C${'\uDDE6' + index}") + " ")
            }
            appendTurns(this, emotes)
        }.toString()).queue { message = it }
    }

    override fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message) { }
}