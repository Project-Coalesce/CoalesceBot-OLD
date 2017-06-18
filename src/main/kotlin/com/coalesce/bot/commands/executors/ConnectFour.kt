package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.*
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent

private val allEmotes = arrayOf("⚫", "🔵", "🔴", "⚪")
private val columns = 7
private val rows = 6

class ConnectFour @Inject constructor(val bot: Main) {
    private val game = ConnectFourGame()

    @RootCommand(
            name = "ConnectFour",
            type = CommandType.FUN,
            permission = "commands.connect4",
            description = "Play a connect four game!",
            aliases = arrayOf("connect4", "con4"),
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

class ConnectFourGame: ChatGame("ConnectFour", 4.0, allEmotes.size - 2) {
    override fun generateMatch(channel: MessageChannel, players: Array<User>, resultHandler: (Map<User, Int>) -> Unit): ChatMatch = ConnectFourMatch(channel, this, players, resultHandler)
}

class ConnectFourMatch(
        channel: MessageChannel,
        chatGame: ConnectFourGame,
        players: Array<User>,
        resultHandler: (positions: Map<User, Int>) -> Unit
): TurnChatMatch(channel, chatGame, players, resultHandler) {
    private val emotes = mutableMapOf<User, String>().apply { players.forEachIndexed { i, it -> this[it] = allEmotes[i] } }
    private val playerEmotes = mutableMapOf<String, User>().apply { players.forEachIndexed { i, it -> this[allEmotes[i]] = it } }
    private val board = mutableListOf<MutableList<Block>>().apply {
        for (x in 1..rows) {
            add(mutableListOf<Block>().apply {
                for (y in 1..columns) {
                    add(Block(null))
                }
            })
        }
    }
    private val topMessage = StringBuilder().apply { for (x in 1..columns) append("${'\u0030' + x}\u20E3") }.toString()
    private var message: Message? = null

    init {
        sendUpdateMessage()
    }

    private fun detectVictory(): Boolean {
        fun analyseAll(pieces: List<Block>): String? {
            var amount = 0
            var emote = ""
            pieces.forEach {
                if (it.emote == null) {
                    emote = ""
                    amount = 0
                    return@forEach
                }
                if (it.emote != emote) {
                    emote = it.emote!!
                    amount = 1
                    return@forEach
                }
                amount ++
                if (amount == 4) {
                    return@analyseAll emote
                }
            }
            return null
        }
        fun win(response: String?): Boolean {
            if (response != null) {
                val map = mutableMapOf<User, Int>()
                players.forEach { map[it] = if (playerEmotes[response] == it) 1 else 0 }
                invoke(map)
                return true
            }
            return false
        }

        board.forEach { if (win(analyseAll(it))) return true } // Horizontally

        // Vertically
        for (x in 1..columns) if (win(analyseAll(mutableListOf<Block>().apply {
            for (y in 1..rows) {
                add(board[y][x])
            }
        } ))) return true

        // Diagonally
        for (rowStart in 0..rows - 4) {

        }


        if (board.all { it.all { it.emote != null } }) {
            val map = mutableMapOf<User, Int>()
            players.forEach { map[it] = 0 }
            invoke(map)
            return true
        }
        return false
    }

    override fun messaged(from: User, content: String): Boolean {
        keepAlive()
        if (!isNext(from)) return false
        val numb = (content.toIntOrNull() ?: return false) - 1
        if (numb !in 0..columns - 1) return false

        var acted = false
        board.forEachIndexed { index, blocks ->
            if (blocks[numb].emote != null) {
                if (index == 0) return true
                board[index - 1][numb].emote = emotes[from]!!
                acted = true
                return@forEachIndexed
            }
        }
        if (!acted) board[rows - 1][numb].emote = emotes[from]!!

        if (!detectVictory()) nextTurn()
        sendUpdateMessage()

        return true
    }

    private fun sendUpdateMessage() {
        if (message != null) message!!.delete().queue()
        channel.sendMessage(StringBuilder().apply {
            append(topMessage)
            board.forEach {
                append("\n")
                it.forEach { append(it.emote ?: "◻") }
            }

        }.toString()).queue { message = it }
    }

    override fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message) { }

    data class Block(var emote: String? = null)
}