package com.coalesce.bot.`fun`

import com.coalesce.bot.CachedDataManager
import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.dataDirectory
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.timeOutHandler
import net.dv8tion.jda.core.entities.User
import java.io.File
import java.util.concurrent.TimeUnit

class MarketplaceManager: CachedDataManager<String, MarketplaceItem>(marketplaceFile, MarketplaceSerializer(marketplaceFile),
        { throw ArgsException("That meme doesn't exist!") }) {
    private val userCache = mutableMapOf<Long, MarketplaceUser>()

    operator fun get(from: User): MarketplaceUser {
        return userCache[from.idLong] ?: run {
            val dataFile = File(dataDirectory, "shop_${from.idLong}.json")
            val userData = if (dataFile.exists()) gson.fromJson(dataFile.readText(), MarketplaceUser::class.java) else
                MarketplaceUser(mutableListOf())
            userCache[from.idLong] = userData
            timeOutHandler(1L, TimeUnit.HOURS) { userCache.remove(from.idLong) }
            userData
        }
    }

    fun exists(name: String) = rawData.containsKey(name)
}

class MarketplaceItem(val imageURL: String, val text: String, val price: Int, val creation: Long, val owner: Long, var purchases: Int)
class MarketplaceUser(private val marketItems: MutableList<MarketplaceItem>) {
    val items: List<MarketplaceItem>
        get() = marketItems

    fun addItem(item: MarketplaceItem, user: User) {
        marketItems.add(item)

        val file = File(dataDirectory, "shop_${user.idLong}.json")
        if (file.exists()) file.delete()
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText(gson.toJson(this))
    }
}