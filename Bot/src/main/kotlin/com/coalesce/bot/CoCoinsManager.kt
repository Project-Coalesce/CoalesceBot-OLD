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
    var rawData: MutableMap<Long, CoCoinsValue>
        get() = serializer.read()
        set(map) = serializer.write(map)

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

    internal fun save(user: User, value: CoCoinsValue) {
        val map = rawData
        map[user.idLong] = value
        cache[user.idLong] = value
        rawData = map
    }

    fun clearCache() = cache.clear()
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