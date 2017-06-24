package com.coalesce.bot.cocoins

import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.dataDirectory
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.timeOutHandler
import net.dv8tion.jda.core.entities.User
import java.io.File
import java.util.concurrent.TimeUnit

class MarketplaceManager {
    private val itemCache = mutableMapOf<String, MarketplaceItem>()
    private val userCache = mutableMapOf<Long, MarketplaceUser>()

    init {
        val file = marketplaceFile
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(gson.toJson(itemCache))
        }
    }
    var rawItemData: MutableMap<String, MarketplaceItem>
        get() = gson.fromJson(marketplaceFile.readText(), itemCache.javaClass)
        set(map) {
            val file = marketplaceFile
            if (file.exists()) file.delete()
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText(gson.toJson(map))
        }

    operator fun set(name: String, item: MarketplaceItem) {
        itemCache[name] = item
        timeOutHandler(1L, TimeUnit.HOURS) { itemCache.remove(name) }
        rawItemData = rawItemData.apply { put(name, item) }
    }

    operator fun get(from: String): MarketplaceItem {
        return itemCache[from] ?: run {
            val data = rawItemData[from] ?: throw ArgsException("Meme not found!")
            itemCache[from] = data
            timeOutHandler(1L, TimeUnit.HOURS) { itemCache.remove(from) }
            data
        }
    }

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

    internal fun save(user: User, value: MarketplaceUser) {
        val file = File(dataDirectory, "shop_${user.idLong}.json")
        if (file.exists()) file.delete()
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText(gson.toJson(value))
    }
}

class MarketplaceItem(val manager: MarketplaceManager, val imageURL: String, val text: String, val price: Int, val creation: Long, val owner: Long, var purchases: Int)
class MarketplaceUser(private val marketItems: MutableList<MarketplaceItem>) {
    val items: List<MarketplaceItem>
        get() = marketItems

    fun addItem(item: MarketplaceItem, manager: MarketplaceManager, user: User) {
        marketItems.add(item)
        manager.save(user, this)
    }
}