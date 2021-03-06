package com.coalesce.bot.currencies.commands

import com.coalesce.bot.CoCoinsTransaction
import com.coalesce.bot.experience.ExperienceCachedDataManager
import com.coalesce.bot.Main
import com.coalesce.bot.currencies.memesChannel
import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.*
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Emote
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@Command("CoalesceCoins", "balance bal money cocoins coins coc cocs coccs")
@UserCooldown(12L)
class CoalesceCoins @Inject constructor(jda: JDA, val main: Main, val experienceDataManager: ExperienceCachedDataManager): Embeddables {
    data class MemeReaction(val message: String,
                             val amount: Double,
                             val delay: Double,
                            val rating: String,
                            val emote: Emote? = null,
                            val stringEmote: String? = null)
    private val memeReactions = mutableListOf(
            MemeReaction("Not Dank Enough", -1.0, 1260.0, "0/10", emote = jda.getEmoteById(304043388523511808L)),
            MemeReaction("Funny 🥚🥚🇩 🇪", 1.0, 860.0, "6.9/10", stringEmote = "😂"),
            MemeReaction("Lit Fam", 2.0, 720.0, "8.5/10", stringEmote = "🔥"),
            MemeReaction("Dank", 3.0, 1260.0, "10/10", emote = jda.getEmoteById(318557118791680000L))
    )

    @CommandAlias("Find out how much money someone has")
    fun coCoins(context: CommandContext, target: User = context.author) {
        val coins = context.main.coCoinsManager[target]

        val transactionsString = if (coins.transactions.isEmpty()) "None." else
            coins.transactions.joinToString(separator = "\n") { "**${if (it.amount >= 0) "+" else ""}${it.amount.toInt()}**: ${it.message}" }

        context(embed().apply {
            embColor = Color(0x5ea81e)
            embTitle = "${if (target == context.author) "You have" else "${target.name} has"} ${coins.total.toInt()} CoCoins."
            field("Recent", transactionsString, false)
        })
    }

    @SubCommand("top", "leaderboard top", "View the CoCoins leaderboard")
    @GlobalCooldown(20L, TimeUnit.SECONDS)
    fun coinsBoard(context: CommandContext, page: Int = 1) {
        val coins = context.main.coCoinsManager
        val board = coins.rawData
        if (page * 10 > board.size + 10 || page < 1) throw ArgsException("That page is out of bounds.")
        val list = board.entries.toList().filter { context.main.jda.getUserById(it.key) != null }
                .order { o1, o2 -> (o1.value.total - o2.value.total).toInt() }.subList((page - 1) * 10, Math.min(board.size, page * 10))
        val amountPositions = list.map { it.value.total }.order(Comparator.comparingInt(Double::toInt).reversed())

        context(embed().apply {
            embTitle = "CoalesceCoins Leaderboard (Page $page/${BigDecimal(board.size / 10.0).setScale(0, BigDecimal.ROUND_UP).toInt()})"
            description {
                list.forEach {
                    val pos = amountPositions.indexOf(it.value.total)
                    append("**${pos.nth()} Place:**")
                    if (it.key == context.author.idLong)
                    append(context.guild.getMember(context.main.jda.getUserById(it.key)).effectiveName)
                    append("[${it.value.total.toInt()}¢$]\n")
                }
            }
        })
    }

    @JDAListener
    fun messageReceive(event: MessageReceivedEvent) {
        if (event.channel.idLong == memesChannel && !event.author.isBot &&
                (event.message.attachments.any() || event.message.rawContent.containsUrl())) {
            event.channel.getMessageById(event.messageIdLong).queue { message ->
                memeReactions.forEach {
                    if (it.stringEmote != null) message.addReaction(it.stringEmote).queue()
                    else message.addReaction(it.emote!!).queue()
                }
            }
        }

        val user = event.author
        if (user.isBot || event.channel !is TextChannel) return
        experienceDataManager.expAdd(user, 1, main.coCoinsManager[user.idLong], event.channel as TextChannel)
    }

    @ReactionListener("MemeReaction", arrayOf("memeReactionCheck"))
    @UserCooldown(10L, TimeUnit.MINUTES)
    fun reactionReceive(context: ReactionContext) {
        context.channel.getMessageById(context.message).queue {
            if (it.author == context.author || it.author.isBot || context.author.isBot
                    || context.channel.idLong != memesChannel) return@queue
            val coins = context.main.coCoinsManager[it.author]
            val reaction = memeReactions.find { (it.stringEmote ?: it.emote) == (context.emote.emote ?: context.emote.name) }!!
            coins.transaction(CoCoinsTransaction("Meme reaction (${context.author.name}): \"${reaction.message}\" - ${reaction.rating}",
                    reaction.amount), context.channel, it.author)
        }
    }

    // Checks for memes
    fun memeReactionCheck(event: ReactionContext) = !event.author.isBot &&
            memeReactions.any { (it.stringEmote ?: it.emote) == (event.emote.emote ?: event.emote.name) }
}