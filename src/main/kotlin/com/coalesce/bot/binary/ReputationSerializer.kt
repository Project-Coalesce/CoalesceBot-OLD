package com.coalesce.bot.binary

import com.coalesce.bot.reputation.ReputationTransaction
import com.coalesce.bot.reputation.ReputationValue
import java.io.File

class ReputationSerializer(file: File): BinarySerializer<MutableMap<Long, ReputationValue>>(file) {
    override fun serializeIn(): MutableMap<Long, ReputationValue> {
        val map = mutableMapOf<Long, ReputationValue>()

        var long: Long
        while (true) {
            long = inputStream.readLong()
            if (long == -1L) break

            val total = inputStream.readDouble()
            val transactions = mutableListOf<ReputationTransaction>()
            val milestones = mutableListOf<String>()

            val transactionCount = inputStream.readInt()
            var numb = 0
            while (true) {
                if (numb >= transactionCount) break
                val amount = inputStream.readDouble()
                val message = inputStream.readUTF()
                transactions.add(ReputationTransaction(message, amount))
                ++numb
            }
            val milestoneCount = inputStream.readInt()
            numb = 0
            while (true) {
                if (numb >= milestoneCount) break
                milestones.add(inputStream.readUTF())
                ++numb
            }

            map[long] = ReputationValue(total, transactions, milestones)
        }

        return map
    }

    override fun serializeOut(data: MutableMap<Long, ReputationValue>) {
        data.forEach { k, v ->
            outputStream.writeLong(k)
            outputStream.writeDouble(v.total)

            outputStream.writeInt(v.transactions.size)
            v.transactions.forEach {
                outputStream.writeDouble(it.amount)
                outputStream.writeUTF(it.message)
            }

            outputStream.writeInt(v.milestones.size)
            v.milestones.forEach {
                outputStream.writeUTF(it)
            }
        }
        outputStream.writeLong(-1L)
    }

}