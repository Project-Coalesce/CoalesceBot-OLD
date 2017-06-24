package com.coalesce.bot.cocoins.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.*
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@Command("CoalesceCoins", "balance bal money cocoins coins coc")
@UserCooldown(12L, TimeUnit.SECONDS)
class CoalesceCoins: Embeddables {
    @CommandAlias("Find out how much money someone has")
    fun coCoins(context: CommandContext, target: User = context.author) {
        val coins = context.main.coCoinsManager[target]

        val transactionsString = if (coins.transactions.isEmpty()) "None." else
            coins.transactions.joinToString(separator = "\n") { "**${if (it.amount >= 0) "+" else ""}${it.amount.toInt()}**: ${it.message}" }

        context(embed().apply {
            embColor = Color(0x5ea81e)
            embTitle = "${if (target == context.author) "You have" else "${target.name} has"} ${coins.total.toInt()} reputation."
            field("Recent", transactionsString, false)
        })
    }

    @SubCommand("top", "leaderboard top")
    @GlobalCooldown(20L, TimeUnit.SECONDS)
    fun coinsBoard(context: CommandContext, page: Int = 1) {
        val coins = context.main.coCoinsManager
        val board = coins.rawData
        if (page * 10 > board.size) throw ArgsException("That page is out of bounds.")
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
}