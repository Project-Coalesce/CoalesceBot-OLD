package com.coalesce.bot.reputation

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel

val milestoneList = arrayOf<ReputationMilestone>(
        DownvoteMilestone()
)

class ReputationValue(var total: Double, var transactions: MutableList<ReputationTransaction>, val milestones: MutableList<ReputationMilestone>) {
    fun transaction(transaction: ReputationTransaction, channel: MessageChannel, member: Member) {
        transactions.add(transaction)
        if (transactions.size > 10) transactions = transactions.subList(0, 10)

        channel.sendMessage("${member.asMention}: ${transaction.message}\n" +
                "**${if (transaction.amount >= 0) "+" else ""}${transaction.amount.toInt()} reputation!**").queue()
        total += transaction.amount

        milestoneList.forEach {
            if (!milestones.contains(it) && total >= it.rep) {
                it.reached(member, channel)
            } else if (milestones.contains(it) && total < it.rep) {
                it.lost(member, channel)
            }
        }
    }
}

class ReputationTransaction(val message: String, val amount: Double)