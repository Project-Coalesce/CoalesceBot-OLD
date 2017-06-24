package com.coalesce.bot.binary

import com.coalesce.bot.CoCoinsTransaction
import com.coalesce.bot.CoCoinsValue
import java.io.File

class CoCoinsSerializer(file: File): BinarySerializer<MutableMap<Long, CoCoinsValue>>(file, { mutableMapOf() }) {
    override fun serializeIn(): MutableMap<Long, CoCoinsValue> {
        val map = mutableMapOf<Long, CoCoinsValue>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break

            val total = inputStream.readDouble()
            val transactions = mutableListOf<CoCoinsTransaction>()

            val transactionCount = inputStream.readInt()
            var numb = 0
            while (true) {
                if (numb >= transactionCount) break
                val amount = inputStream.readDouble()
                val message = inputStream.readUTF()
                transactions.add(CoCoinsTransaction(message, amount))
                ++numb
            }

            map[long] = CoCoinsValue(total, transactions)
        }

        return map
    }

    override fun serializeOut(data: MutableMap<Long, CoCoinsValue>) {
        data.forEach { k, v ->
            outputStream.writeLong(k)
            outputStream.writeDouble(v.total)

            outputStream.writeInt(v.transactions.size)
            v.transactions.forEach {
                outputStream.writeDouble(it.amount)
                outputStream.writeUTF(it.message)
            }
        }
        outputStream.writeLong(-1L)
    }
}