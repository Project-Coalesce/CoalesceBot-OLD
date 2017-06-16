package com.coalesce.bot.commands

import com.coalesce.bot.binary.RespectsLeaderboardSerializer
import com.coalesce.bot.respectsLeaderboardsFile
import com.coalesce.bot.utilities.Timeout
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.util.concurrent.TimeUnit

private val checks = arrayOf<(match: MatchLooking, user: User) -> Pair<Boolean, String>>(
        { match, user -> (match.targets != null && !match.targets.contains(user)) to "You cannot join this private match." },
        { match, user ->
            (match.bidding != 0.0 && match.bidding > RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()[user.id] ?: 0.0) to
                    "You don't have enough respects to afford this bid!" },
        { match, user -> (user == match.looker) to "You can't join your own match." }
)

abstract class ChatGame(val name: String, val defaultAward: Double, val maxPlayers: Int): Embeddables {
    val game = mutableMapOf<User, ChatMatch>()
    val reactionPossible = mutableListOf<Long>()
    val matchfinding = mutableMapOf<Long, MatchLooking>() // Matchfinder message (Reaction), Pair where <Attempting to find, Specific target if present>

    // Syntax:
    // !<game> - Finds match without bet for 1 player
    // !<game> <amount> - Finds match without bet for <amount> players
    // !<game> bet <betamount> - Finds match betting <betamount> for 1 player
    // !<game> bet <betamount> <amount> - Finds match betting <betamount> for <amount> players
    // !<game> <players> - Challenges <players> into a match
    // !<game> bet <betamount> <players> - Challenges <players> into a match betting <betamount>
    fun handleCommand(context: CommandContext) {
        fun matchfinding(targeting: List<User>?, bid: Double, earnAmount: Double, amount: Int) {
            context(embed().apply {
                setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                setTitle(if (targeting != null) "Challenged ${targeting.joinToString(separator = ", ") { it.name }} to a $name match!" else "Looking for a $name match", null)
                setDescription(StringBuilder().apply {
                    if (bid > 0.0) append("Must use $bid respects to join.\n")
                    append("React with 🚪 to ${if (targeting != null) "accept" else "join"}!\nWinner gets **$earnAmount respects**")
                }.toString())
            }.build()) {
                matchfinding[idLong] = MatchLooking(context.author, targeting, bid, mutableListOf(), amount, System.currentTimeMillis())
                addReaction("🚪").queue()
            }
        }
        if (game.containsKey(context.author)) throw ArgsException("You are already on a match!")

        if (context.args.isEmpty()) {
            matchfinding(null, 0.0, defaultAward, 1)
        } else if (context.message.mentionedUsers.isNotEmpty()) {
            val target = context.message.mentionedUsers

            if (context.args.size > 2 && context.args[0] == "bet") {
                val bid = context.args[1].toDoubleOrNull() ?: throw ArgsException("The provided bid is an invalid number!")
                if (bid > 30.0) throw ArgsException("Only bids up to 30 respects are allowed.")
                if (game.containsKey(context.author)) throw ArgsException("You are already on a match!")
                if (bid > RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()[context.author.id] ?: 0.0)
                    throw ArgsException("You can't afford that bid!")

                matchfinding(target, bid, bid, target.size)
            } else {
                matchfinding(target, 0.0, defaultAward, target.size)
            }
        } else if (context.args[0] == "bet" && context.args.size > 1){
            val bid = context.args[0].toDoubleOrNull() ?: throw ArgsException("The provided bid is an invalid number!")
            val amount = if (context.args.size > 2) context.args[2].toIntOrNull() ?: throw ArgsException("The provided amount of players is not a valid number!") else 0
            if (amount !in 1..maxPlayers) throw ArgsException("Amount of players must be within 1 and $maxPlayers.")
            if (bid > 30.0) throw ArgsException("Only bids up to 30 respects are allowed.")
            if (game.containsKey(context.author)) throw ArgsException("You are already on a match!")
            if (bid > RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()[context.author.id] ?: 0.0)
                throw ArgsException("You can't afford that bid!")

            matchfinding(null, bid, bid, amount)
        } else {
            val amount = context.args[0].toIntOrNull() ?: throw ArgsException("The provided amount of players is not a valid number!")
            matchfinding(null, 0.0, defaultAward, amount)
        }
    }

    fun handleReaction(event: MessageReactionAddEvent) {
        if (matchfinding.containsKey(event.messageIdLong)) {
            val match = matchfinding[event.messageIdLong]!!
            checks.forEach {
                val (fail, message) = it(match, event.user)
                if (fail) {
                    event.channel.sendMessage("${event.user.asMention} ❌: $message").queue()
                    return@handleReaction
                }
            }

            match.entered.add(event.user)
            event.channel.sendMessage("${event.user.asMention}: Joined queue for $name match! (${match.entered.size}/${match.amount})").queue()
            if (match.entered.size == match.amount) {
                val players = mutableListOf(event.user).apply { addAll(match.entered) }
                val worth = if (match.bidding > 0.0) match.bidding else defaultAward
                event.channel.getMessageById(event.messageId).queue { it.delete().queue() }
                event.channel.sendMessage("**$name Match Created!**\n${players.joinToString(separator = " vs ") { it.name }}" +
                        "\nWinner will get **$worth respects**.\n**Good luck!**").queue()

                val gameMatch = generateMatch(event.channel, players.toTypedArray()) { results ->
                    event.channel.sendMessage(StringBuilder("**The match has ended!**\nResults:").apply {
                        val map = RespectsLeaderboardSerializer(respectsLeaderboardsFile).read()
                        players.forEach {
                            val pos = results[it] ?: 0
                            if (pos == 1) {
                                map[it.id] = (map[it.id] ?: 0.0) + worth
                                append("\n${it.asMention}: Congratulations, you won! **+$worth respects**")
                            } else if (match.bidding > 0.0) {
                                map[it.id] = (map[it.id] ?: 0.0) - match.bidding
                                append("\n${it.asMention}: You lost the bet! **-${match.bidding} respects**")
                            }
                            else append("\n${it.asMention}: You didn't get anything. Maybe next time!")
                        }

                        RespectsLeaderboardSerializer(respectsLeaderboardsFile).write(map)
                    }.toString()).queue()
                }

                players.forEach { game[it] = gameMatch }
            }
        }else if (game.containsKey(event.user) && reactionPossible.contains(event.messageIdLong)) event.channel.getMessageById(event.messageId).queue {
            game[event.user]!!.reaction(event.user, event.reactionEmote, it)
        }
    }

    fun handleMessage(event: MessageReceivedEvent) {
        if (game.containsKey(event.author)) if (game[event.author]!!.messaged(event.author, event.message.rawContent)) event.message.delete().queue()
    }

    abstract fun generateMatch(channel: MessageChannel, players: Array<User>, resultHandler: (Map<User, Int>) -> Unit): ChatMatch
}

data class MatchLooking(val looker: User,
                        val targets: List<User>?,
                        val bidding: Double,
                        val entered: MutableList<User>,
                        val amount: Int,
                        val begin: Long)

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