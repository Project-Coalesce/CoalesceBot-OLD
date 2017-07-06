package com.coalesce.bot

import com.coalesce.bot.binary.CoCoinsSerializer
import com.coalesce.bot.utilities.timeOutHandler
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.io.DataOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

class CoCoinsManager: CachedDataManager<Long, CoCoinsValue>(coCoinsFile, CoCoinsSerializer(coCoinsFile), { CoCoinsValue(0.0, mutableListOf()) }) {
    operator fun get(from: User) = get(from.idLong)
    internal fun save(user: User, value: CoCoinsValue) = save(user.idLong, value)
}

class CoCoinsValue(var total: Double, var transactions: MutableList<CoCoinsTransaction>) {
    fun transaction(transaction: CoCoinsTransaction, channel: TextChannel, user: User) {
        val member = channel.guild.getMember(user)
        transactions.add(0, transaction)
        if (transactions.size > 10) transactions = transactions.subList(0, 10)

        channel.sendMessage("${member.asMention}: ${transaction.message}\n" +
                "**${if (transaction.amount >= 0) "+" else ""}${transaction.amount.toInt()}¢$!**").queue()
        total += transaction.amount

        Main.instance.coCoinsManager.save(user, this)
    }
}

data class CoCoinsTransaction(val message: String, val amount: Double)