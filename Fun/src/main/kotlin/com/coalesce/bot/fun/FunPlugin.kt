package com.coalesce.bot.`fun`

import com.coalesce.bot.command.Plugin
import com.coalesce.bot.dataDirectory
import java.io.File

class FunPlugin: Plugin() {
    lateinit var marketplaceManager: MarketplaceManager

    override fun onRegister() {
        marketplaceManager = MarketplaceManager()
        addGuiceInjection(marketplaceManager::class.java, marketplaceManager)
    }
}

val marketplaceFile = File(dataDirectory, "marketplace.json")