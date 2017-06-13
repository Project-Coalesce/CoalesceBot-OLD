package com.coalesce.bot.reputation

import com.coalesce.bot.binary.ReputationSerializer
import com.coalesce.bot.reputationFile
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.io.DataOutputStream
import java.io.File

class ReputationManager {
    private val cache = mutableMapOf<Long, ReputationValue>()
    private val serializer : ReputationSerializer

    init {
        val classes = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner(false), ResourcesScanner())
                .setUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(FilterBuilder().include(FilterBuilder.prefix("com.coalesce.bot.reputation"))))
                .getSubTypesOf(ReputationMilestone::class.java).filter { !it.name.contains('$') }
        classes.forEach {
            milestoneList.add(it.newInstance())
        }

        val file = reputationFile

        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists())  generateFile(file)

        serializer = ReputationSerializer(file)
    }

    fun generateFile(file: File) {
        file.createNewFile()
        file.outputStream().use {
            DataOutputStream(it).writeLong(-1L)
        }
    }

    operator fun set(user: User, value: ReputationValue) {
        val storage = serializer.read()
        storage[user.idLong] = value
        serializer.write(storage)
    }

    operator fun get(from: User): ReputationValue {
        return cache[from.idLong] ?: run {
            val userData = serializer.read()[from.idLong]
            if (userData != null) cache[from.idLong] = userData
            userData
        } ?: ReputationValue(0.0, mutableListOf<ReputationTransaction>(), mutableListOf<String>())
    }

    fun clearCache() = cache.clear()
}

val milestoneList = mutableListOf<ReputationMilestone>()

class ReputationValue(var total: Double, var transactions: MutableList<ReputationTransaction>, val milestones: MutableList<String>) {
    fun transaction(transaction: ReputationTransaction, channel: MessageChannel, member: Member) {
        transactions.add(0, transaction)
        if (transactions.size > 10) transactions = transactions.subList(0, 10)

        channel.sendMessage("${member.asMention}: ${transaction.message}\n" +
                "**${if (transaction.amount >= 0) "+" else ""}${transaction.amount.toInt()} reputation!**").queue()
        total += transaction.amount

        milestoneList.forEach {
            if (!milestones.contains(it.name) && total >= it.rep) {
                it.reached(member, channel)
                milestones.add(it.name)
            } else if (milestones.contains(it.name) && total < it.rep) {
                it.lost(member, channel)
                milestones.remove(it.name)
            }
        }
    }
}

class ReputationTransaction(val message: String, val amount: Double)