package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.`fun`.winDetection
import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.JDAListener
import com.coalesce.bot.command.game.CoGame
import com.coalesce.bot.command.game.CoTurnMatch
import com.coalesce.bot.utilities.listOf
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent

private val characters = listOf("⚫", "🔵", "🔴","⚪")

@Command("Connect4", "connectfour")
class Connect4 {
    private val game = object: CoGame("ConnectFour", 3, characters.size - 2) {
        override fun match(channel: TextChannel, players: List<User>, resultHandler: (Map<User, Int>) -> Unit) =
                Connect4Match(channel, this, players, resultHandler)
    }

    @CommandAlias("Find a tictactoe match")
    fun exec(context: CommandContext, target: List<User> = listOf(), targetAmount: Int = if (target.isEmpty()) 1 else target.size,
             bid: Int? = null) = game.matchfinding(context, target, targetAmount, bid)

    @JDAListener
    fun messaged(event: MessageReceivedEvent) = game.message(event)

    @JDAListener
    fun reactionAdd(event: GuildMessageReactionAddEvent) = game.react(event)
}

class Connect4Match(
        channel: TextChannel,
        chatGame: CoGame,
        players: List<User>,
        resultHandler: (Map<User, Int>) -> Unit
): CoTurnMatch(channel, chatGame, players, resultHandler) {
    private val columns = 6
    private val rows = 5
    private val emotes = mutableMapOf<User, String>().apply { players.forEachIndexed { i, it -> this[it] = characters[i] } }
    private val board = listOf<MutableList<String?>> { for (y in 0..columns) add(listOf<String?> { for (x in 0..rows) add(null) }) }
    private var message: Message? = null

    init {
        sendUpdateMessage()
    }

    private fun detectVictory(): Boolean {
        val result = winDetection(board, rows - 1, columns - 1, 4)
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
        val numb = (content.toIntOrNull() ?: return false) - 1
        if (numb !in 0..columns) return false
        val line = board[numb]
        val index = line.indexOfLast { it != null } + 1
        if (index > rows) return true
        line[index] = emotes[from]

        if (!detectVictory()) nextTurn()
        sendUpdateMessage()
        return true
    }

    private fun sendUpdateMessage() {
        if (message != null) message!!.delete().queue()
        channel.sendMessage(StringBuilder().apply {
            for (i in 0..columns) append("${'\u0030' + (i + 1)}\u20E3")
            appendln()

            for (x in (0..rows).reversed()) {
                for (y in 0..columns) {
                    append(board[y][x] ?: "⬜")
                }
                appendln()
            }

            appendTurns(this, emotes)
        }.toString()).queue { message = it }
    }

    override fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message) { }
}