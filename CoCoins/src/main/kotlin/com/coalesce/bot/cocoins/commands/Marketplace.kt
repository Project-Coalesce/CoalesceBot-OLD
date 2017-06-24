package com.coalesce.bot.cocoins.commands

import com.coalesce.bot.CoCoinsTransaction
import com.coalesce.bot.cocoins.MarketplaceItem
import com.coalesce.bot.cocoins.MarketplaceManager
import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embTitle
import com.coalesce.bot.utilities.order
import com.google.inject.Inject
import java.util.concurrent.TimeUnit

@Command("Market", "mememarket memes meme marketplace")
@UserCooldown(10L)
class Marketplace @Inject constructor(val marketplaceManager: MarketplaceManager): Embeddables {
    @CommandAlias("Use a dank maymay!")
    fun use(context: CommandContext, memeName: String) {
        val meme = marketplaceManager[memeName]
        val user = marketplaceManager[context.author]
        if (!user.items.contains(meme)) throw ArgsException("You don't own that maymay!")
        context(embed().apply {
            embTitle = meme.text
            setImage(meme.imageURL)
        })
    }

    @SubCommand("Create", "new sell", "Sell memes in the market.")
    @UserCooldown(10L, TimeUnit.MINUTES)
    fun create(context: CommandContext, name: String, image: String, price: Int, @VarArg title: String) {
        if (price !in 5..100) throw ArgsException("A meme's cost should be between 5 and 100.")
        val meme = MarketplaceItem(marketplaceManager, image, title, price, System.currentTimeMillis(), context.author.idLong, 0)
        marketplaceManager[name] = meme
        context("Meme added.")
    }

    @SubCommand("Buy", "find shop", "Take a look at the marketplace.")
    @UserCooldown(10L, TimeUnit.SECONDS)
    fun shop(context: CommandContext) {
        val user = marketplaceManager[context.author]
        val items = marketplaceManager.rawItemData.entries.toList().filter { !user.items.contains(it.value) }
        context(StringBuilder().apply {
            fun namer(it: Map.Entry<String, MarketplaceItem>) = "${it.key} [${it.value.price}¢$] by ${context.main.jda.getUserById(it.value.owner)?.name}"
            fun basedOn(func: (Map.Entry<String, MarketplaceItem>, Map.Entry<String, MarketplaceItem>) -> Int) =
                append(items.order(func).subList(0, Math.min(items.size, 5)).joinToString(separator = "\n",
                        transform = ::namer))
            append("**Meme marketplace**\n")

            append("Recent additions:\n")
            basedOn { o1, o2 -> (o1.value.creation - o2.value.creation).toInt() }
            append("\nTop bought:\n")
            basedOn { o1, o2 -> o1.value.purchases - o2.value.purchases }
        }.toString())
    }

    @SubCommandAlias("Buy", "Search for a specific meme to buy.")
    fun query(context: CommandContext, query: String) {
        val item = marketplaceManager[query]
        val coins = context.main.coCoinsManager
        val bal = coins[context.author]
        if (item.price > bal.total) throw ArgsException("You can't afford that meme.")
        val owner = context.main.jda.getUserById(item.owner) ?: throw ArgsException("Seems like the creator of that meme abandoned the server.")

        coins[owner].transaction(CoCoinsTransaction("Your meme ${item.text} was purchased by ${context.author.name}.", item.price.toDouble()),
                context.channel, context.author)
        bal.transaction(CoCoinsTransaction("Purchased **${item.text}** meme", -item.price.toDouble()), context.channel, context.author)
        marketplaceManager[context.author].addItem(item, marketplaceManager, context.author)
        item.purchases ++
    }
}