package com.coalesce.bot

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel

class ReputationValue(var total: Double, var transactions: MutableList<ReputationTransaction>){
    fun transaction(transaction: ReputationTransaction, channel: MessageChannel, member: Member) {
        transactions.add(transaction)
        if (transactions.size > 10) transactions = transactions.subList(0, 10)

        channel.sendMessage("${member.asMention}: ${transaction.message}\n" +
                "**${if (transaction.amount >= 0) "+" else ""}${transaction.amount.toInt()} reputation!**").queue()
        total += transaction.amount
    }
}

class ReputationTransaction(val message: String, val amount: Double)