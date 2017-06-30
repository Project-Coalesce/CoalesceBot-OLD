package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.command.game.CoGame
import com.coalesce.bot.command.game.CoTurnMatch
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent

@Command("Monopoly")
class Monopoly {
    private val game = object: CoGame("Monopoly", 3, 1, 3) {
        override fun match(channel: TextChannel, @VarArg players: List<User>, resultHandler: (Map<User, Int>) -> Unit) =
                MonopolyMatch(channel, this, players, resultHandler)
    }

    @CommandAlias("Find a Monopoly match")
    fun exec(context: CommandContext, target: List<User> = listOf(), targetAmount: Int = if (target.isEmpty()) 1 else target.size,
                     bid: Int? = null) = game.matchfinding(context, target, targetAmount, bid)

    @JDAListener
    fun messaged(event: MessageReceivedEvent) = game.message(event)

    @JDAListener
    fun reactionAdd(event: GuildMessageReactionAddEvent) = game.react(event)
}

class MonopolyMatch(
        channel: TextChannel,
        chatGame: CoGame,
        players: List<User>,
        resultHandler: (Map<User, Int>) -> Unit
): CoTurnMatch(channel, chatGame, players, resultHandler) {
    override fun messaged(from: User, content: String) = false

    override fun reaction(from: User, emote: MessageReaction.ReactionEmote, message: Message) { }
}