package com.coalesce.bot.commands

import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.respectsLeaderboardsFile
import com.coalesce.bot.utilities.Timeout
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.util.concurrent.TimeUnit

abstract class ChatGame(val name: String, val defaultAward: Double): Embeddables {
    val game = mutableMapOf<User, ChatMatch>()
    val reactionPossible = mutableListOf<Long>()
    val matchfinding = mutableMapOf<Long, Triple<User, User?, Double>>() // Matchfinder message (Reaction), Pair where <Attempting to find, Specific target if present>

    fun handleCommand(context: CommandContext) {
        fun matchfinding(targeting: User?, bid: Double, earnAmount: Double) {
            context(embed().apply {
                setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                setTitle(if (targeting != null) "Challenged ${targeting.asMention} to a $name match!" else "Looking for a $name match", null)
                setDescription(StringBuilder().apply {
                    if (bid > 0.0) append("Must use $bid respects to join.\n")
                    append("React with 🚪 to ${if (targeting != null) "accept" else "join"}!\nWinner gets **$earnAmount respects**")
                }.toString())
            }.build()) {
                matchfinding[idLong] = Triple(context.author, targeting, bid)
                addReaction("🚪").queue()
            }
        }

        if (context.args.isEmpty()) {
            if (game.containsKey(context.author)) throw ArgsException("You are already on a match!")
            matchfinding(null, 0.0, defaultAward)
        } else if (context.message.mentionedUsers.isNotEmpty()) {
            val target = context.message.mentionedUsers.first()

            if (context.args.size == 2) {
                val bid = context.args[0].toDoubleOrNull() ?: throw ArgsException("The provided bid is an invalid number!")
                if (bid > 30.0) throw ArgsException("Only bids up to 30 respects are allowed.")
                if (game.containsKey(context.author)) throw ArgsException("You are already on a match!")
                if (bid > RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()[context.author.id] ?: 0.0)
                    throw ArgsException("You can't afford that bid!")

                matchfinding(target, bid, bid)
            } else {
                matchfinding(target, 0.0, defaultAward)
            }
        } else {
            val bid = context.args[0].toDoubleOrNull() ?: throw ArgsException("The provided bid is an invalid number!")
            if (bid > 30.0) throw ArgsException("Only bids up to 30 respects are allowed.")
            if (game.containsKey(context.author)) throw ArgsException("You are already on a match!")
            if (bid > RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()[context.author.id] ?: 0.0)
                throw ArgsException("You can't afford that bid!")

            matchfinding(null, bid, bid)
        }
    }

    fun handleReaction(event: MessageReactionAddEvent) {
        if (matchfinding.containsKey(event.messageIdLong)) {
            val match = matchfinding[event.messageIdLong]!!
            if (event.reactionEmote.name != "🚪" || (match.second != null && event.user != match.second) ||
                    (match.third != 0.0 && match.third > RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()[event.user.id] ?: 0.0)) return
            matchfinding.remove(event.messageIdLong)

            val players = arrayOf(event.user, match.first)
            val worth = if (match.third > 0.0) match.third else defaultAward
            event.channel.getMessageById(event.messageId).queue { it.delete().queue() }
            event.channel.sendMessage("**${name.toLowerCase().capitalize()} Match ready!**\n${players.joinToString(separator = " vs ") { it.name }}" +
                    "\nWinner will get **$worth respects**.\n**Good luck!**").queue()

            val gameMatch = generateMatch(event.channel, players) { results ->
                event.channel.sendMessage(StringBuilder("**The match has ended!**\nResults:").apply {
                    val map = RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()
                    players.forEach {
                        val pos = results[it] ?: 0
                        if (pos == 1) {
                            map[it.id] = (map[it.id] ?: 0.0) + worth
                            append("\n${it.asMention}: Congratulations, you won! **+$worth respects**")
                        } else if (match.third > 0.0) {
                            map[it.id] = (map[it.id] ?: 0.0) - match.third
                            append("\n${it.asMention}: You lost the bet! **-${match.third} respects**")
                        }
                        else append("\n${it.asMention}: You didn't get anything. Maybe next time!")
                    }

                    RespectsLeaderboardSerializer(respectsLeaderboardsFile).write(map)
                }.toString()).queue()
            }

            players.forEach { game[it] = gameMatch }
        }else if (game.containsKey(event.user) && reactionPossible.contains(event.messageIdLong)) event.channel.getMessageById(event.messageId).queue {
            game[event.user]!!.reaction(event.user, event.reactionEmote, it)
        }
    }
    fun handleMessage(event: MessageReceivedEvent) {
        if (game.containsKey(event.author)) if (game[event.author]!!.messaged(event.author, event.message.rawContent)) event.message.delete().queue()
    }

    abstract fun generateMatch(channel: MessageChannel, players: Array<User>, resultHandler: (Map<User, Int>) -> Unit): ChatMatch
}

abstract class ChatMatch(
        val channel: MessageChannel,
        val chatGame: ChatGame,
        val players: Array<User>,
        val resultHandler: (positions: Map<User, Int>) -> Unit
): Timeout(2L, TimeUnit.MINUTES) {
    private val addedReactionsOf = mutableListOf<Long>()

    init {
        timeout = { invoke(mapOf()) }
    }

    abstract fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message)
    abstract fun messaged(from: User, content: String): Boolean

    fun registerMessageReactions(message: Message) = chatGame.reactionPossible.add(message.idLong)

    operator fun invoke(positions: Map<User, Int>) {
        chatGame.reactionPossible.removeAll(addedReactionsOf)
        players.forEach { chatGame.game.remove(it) }
        resultHandler(positions)
    }
}