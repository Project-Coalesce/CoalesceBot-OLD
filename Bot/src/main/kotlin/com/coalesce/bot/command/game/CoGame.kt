package com.coalesce.bot.command.game

import com.coalesce.bot.CoCoinsTransaction
import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.send
import com.coalesce.bot.utilities.*
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import java.util.concurrent.TimeUnit

// Dank Command Framework
abstract class CoGame(val name: String, val winCount: Int, val xp: Int, val minPlayers: Int, val maxPlayers: Int): Embeddables {
    internal val reactionListeners = mutableMapOf<Long, (GuildMessageReactionAddEvent) -> Unit>()
    internal val inMatch = mutableMapOf<Long, CoGameMatch>()
    internal val inFinder = mutableListOf<Long>()

    abstract fun match(channel: TextChannel, players: List<User>, resultHandler: (User?) -> Unit): CoGameMatch

    fun matchfinding(context: CommandContext, target: List<User>, targetAmount: Int, bid: Int?) {
        if (bid != null && bid > context.main.coCoinsManager[context.author].total) throw ArgsException("You don't have enough money to bid that.")
        if (bid != null && bid !in 4..20) throw ArgsException("You can only bid between 4 and 20 coins.")
        if (context.channel.idLong != 326191031412588544L) throw ArgsException("Please use the channel <#326191031412588544>.") // #games
        if (targetAmount !in minPlayers..maxPlayers) throw ArgsException("The amount of players must be between $minPlayers and $maxPlayers.")

        context(embed().apply {
            embTitle = "Looking for a match of $name!"
            description {
                appendln("Click on the door below to join!")
                if (bid != null) appendln("This will cost you $bid.")
                appendln("Winner gets ${bid ?: winCount} CoCoins!")
            }
        }) {
            addReaction("🚪").queue()

            val entered = mutableListOf<User>()
            val timeout = timeOutHandler(3L, TimeUnit.MINUTES) {
                context("The match timer timed out.")
                inFinder.removeAll((entered and context.author).map(User::getIdLong))
                reactionListeners.remove(idLong)
                delete().queue()
            }
            reactionListeners[idLong] = {
                if ((target.isEmpty() || target.contains(it.user)) && (bid == null || bid > context.main.coCoinsManager[it.user].total) &&
                        !(entered.contains(it.user)) && it.reactionEmote.name == "🚪" && context.author != it.user) {
                    timeout.stopTimeout()
                    entered.add(it.user)
                    context("${it.user.asMention} joined the queue! (${entered.size}/$targetAmount)")

                    if (entered.size == targetAmount) {
                        delete().queue()
                        val players = entered and context.author
                        inFinder.removeAll(players.map(User::getIdLong))
                        val match = match(context.channel, players) { winner ->
                            val coins = context.main.coCoinsManager
                            players.forEach {
                                if (it == winner) {
                                    coins[it].transaction(CoCoinsTransaction("Congratulations, you won!", (bid ?: winCount).toDouble()),
                                            context.channel, context.author)
                                } else if (bid != null) {
                                    coins[it].transaction(CoCoinsTransaction("You lost the bet!", (-bid).toDouble()),
                                            context.channel, context.author)
                                }
                                context.main.experienceCachedDataManager.expAdd(it, xp, coins[it], context.channel)
                            }

                        }
                        players.forEach { inMatch[it.idLong] = match }
                    }
                }
            }
        }
    }

    fun message(event: MessageReceivedEvent) {
        if (event.channel !is TextChannel) return
        if (inMatch.containsKey(event.author.idLong)) {
            if (inMatch[event.author.idLong]!!.messaged(event.author, event.message.rawContent)) event.message.delete().queue()
        }
    }
    fun react(event: GuildMessageReactionAddEvent) {
        if (reactionListeners.contains(event.messageIdLong) && !event.member.user.isBot) {
            reactionListeners[event.messageIdLong]!!(event)
        }
    }
}

abstract class CoGameMatch(
        val channel: TextChannel,
        val game: CoGame,
        val players: List<User>,
        val resultHandler: (winner: User?) -> Unit
): Timeout(3L, TimeUnit.MINUTES) {
    private val addedReactionsOf = mutableListOf<Long>()

    override fun timeout() {
        invoke(null, "Nothing happened for the last 2 minutes, so the match will automatically be tied.")
    }

    abstract fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message)
    abstract fun messaged(from: User, content: String): Boolean

    fun registerMessageReactions(message: Message) {
        game.reactionListeners.put(message.idLong) { handler ->
            handler.channel.getMessageById(handler.messageIdLong).queue {
                reaction(handler.user, handler.reactionEmote, it)
            }
        }
        addedReactionsOf.add(message.idLong)
    }

    operator fun invoke(winner: User?, message: String) {
        channel.send("**Game Over!**\n$message\nEveryone earnt **+${game.xp} Experience** as a reward!")
        game.reactionListeners.removeAll(addedReactionsOf)
        players.forEach { game.inMatch.remove(it.idLong) }
        resultHandler(winner)
        stopTimeout()
    }
}

abstract class CoTurnMatch(
        channel: TextChannel,
        chatGame: CoGame,
        players: List<User>,
        resultHandler: (winner: User?) -> Unit
): CoGameMatch(channel, chatGame, players, resultHandler) {
    private var turnQueue = mutableListOf<User>().apply { addAll(players) }

    fun isNext(user: User) = turnQueue[0] == user

    fun nextTurn() {
        val oldFirst = turnQueue[0]
        turnQueue = turnQueue.subList(1).toMutableList()
        turnQueue.add(oldFirst)
    }

    fun appendTurns(builder: StringBuilder, characterMap: Map<User, String>? = null) {
        val curTurn = turnQueue[0]
        players.forEach {
            builder.append("\n")
            if (characterMap != null) builder.append(characterMap[it] + " ")
            if (curTurn == it) builder.append("`${it.name}             «`")
            else builder.append(it.name)
        }
    }
}