package com.coalesce.bot.reputation

import com.coalesce.bot.gson
import com.coalesce.bot.reputationFile
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder

class ReputationManager {
    private val reputationStorage: MutableMap<String, ReputationValue>

    init {
        val classes = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner())
                .setUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(FilterBuilder().include(FilterBuilder.prefix("com.coalesce.bot.reputation"))))
                .getSubTypesOf(milestoneList.javaClass).filter { !it.name.contains('$') }
        classes.forEach {
            it.newInstance().forEach {
                milestoneList.add(it)
            }
        }

        val file = reputationFile

        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("{}")
        }

        val type = object: TypeToken<HashMap<String, ReputationValue>>() {}
        reputationStorage = gson.fromJson<MutableMap<String, ReputationValue>>(reputationFile.readText(), type.type)
    }

    fun save() {
        reputationFile.writeText(gson.toJson(reputationStorage))
    }

    operator fun set(user: User, value: ReputationValue) {
        reputationStorage[user.id] = value
        save()
    }

    operator fun get(from: User): ReputationValue {
        return reputationStorage[from.id] ?: ReputationValue(0.0, mutableListOf<ReputationTransaction>(), mutableListOf<ReputationMilestone>())
    }
}

val milestoneList = mutableListOf<ReputationMilestone>()

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