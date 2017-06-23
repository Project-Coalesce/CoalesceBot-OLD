package com.coalesce.bot

import com.coalesce.bot.binary.CoCoinsSerializer
import com.coalesce.bot.utilities.timeOutHandler
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.io.DataOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

class CoCoinsManager {
    private val cache = mutableMapOf<Long, CoCoinsValue>()
    private val serializer: CoCoinsSerializer

    init {
        val file = coCoinsFile

        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists())  generateFile(file)

        serializer = CoCoinsSerializer(file)
    }

    fun readRawData(): MutableMap<Long, CoCoinsValue> = serializer.read()
    fun writeRawData(map: MutableMap<Long, CoCoinsValue>) = serializer.write(map)

    fun generateFile(file: File) {
        file.createNewFile()
        file.outputStream().use {
            DataOutputStream(it).writeLong(-1L)
        }
    }

    operator fun get(from: User): CoCoinsValue {
        return cache[from.idLong] ?: run {
            val userData = serializer.read()[from.idLong] ?: CoCoinsValue(0.0, mutableListOf())
            cache[from.idLong] = userData
            timeOutHandler(1L, TimeUnit.HOURS) { cache.remove(from.idLong) }
            userData
        }
    }

    fun clearCache() = cache.clear()
}

class CoCoinsValue(var total: Double, var transactions: MutableList<RespectsTransaction>) {
    fun transaction(transaction:  RespectsTransaction, channel: TextChannel, user: User) {
        val member = channel.guild.getMember(user)
        transactions.add(0, transaction)
        if (transactions.size > 10) transactions = transactions.subList(0, 10)

        channel.sendMessage("${member.asMention}: ${transaction.message}\n" +
                "**${if (transaction.amount >= 0) "+" else ""}${transaction.amount.toInt()}¢$!**").queue()
        total += transaction.amount
    }
}

data class RespectsTransaction(val message: String, val amount: Double)