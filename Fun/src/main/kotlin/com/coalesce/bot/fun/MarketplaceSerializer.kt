package com.coalesce.bot.`fun`

import com.coalesce.bot.binary.BinarySerializer
import java.io.File

class MarketplaceSerializer(file: File): BinarySerializer<MutableMap<String, MarketplaceItem>>(file, { mutableMapOf() }) {
    override fun serializeIn(): MutableMap<String, MarketplaceItem> {
        val map = mutableMapOf<String, MarketplaceItem>()

        val long = inputStream.readLong()
        for (i in 0..long) {
            val key = inputStream.readUTF()
            val item = MarketplaceItem(inputStream.readUTF(), inputStream.readUTF(), inputStream.readInt(), inputStream.readLong(),
                    inputStream.readLong(), inputStream.readInt())

            map[key] = item
        }

        return map
    }

    override fun serializeOut(data: MutableMap<String, MarketplaceItem>) {
        outputStream.writeLong(data.size.toLong())
        data.forEach { k, v ->
            outputStream.writeUTF(k)
            outputStream.writeUTF(v.imageURL)
            outputStream.writeUTF(v.text)
            outputStream.writeInt(v.price)
            outputStream.writeLong(v.creation)
            outputStream.writeLong(v.owner)
            outputStream.writeInt(v.purchases)
        }
    }
}