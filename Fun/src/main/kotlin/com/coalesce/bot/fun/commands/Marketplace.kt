package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.utilities.embTitle
import com.coalesce.bot.utilities.order

@com.coalesce.bot.command.Command("Market", "mememarket memes meme marketplace")
@com.coalesce.bot.command.UserCooldown(10L)
class Marketplace @com.google.inject.Inject constructor(val marketplaceManager: com.coalesce.bot.`fun`.MarketplaceManager): com.coalesce.bot.utilities.Embeddables {
    @com.coalesce.bot.command.CommandAlias("Use a dank maymay!")
    fun use(context: com.coalesce.bot.command.CommandContext, memeName: String) {
        val meme = marketplaceManager[memeName]
        val user = marketplaceManager[context.author]
        if (!user.items.contains(meme)) throw com.coalesce.bot.command.ArgsException("You don't own that maymay!")
        context(embed().apply {
            embTitle = meme.text
            setImage(meme.imageURL)
        })
    }

    @com.coalesce.bot.command.SubCommand("Create", "new sell", "Sell memes in the market.")
    @com.coalesce.bot.command.UserCooldown(10L, java.util.concurrent.TimeUnit.MINUTES)
    fun create(context: com.coalesce.bot.command.CommandContext, name: String, image: String, price: Int, @com.coalesce.bot.command.VarArg title: String) {
        if (price !in 5..100) throw com.coalesce.bot.command.ArgsException("A meme's cost should be between 5 and 100.")
        val meme = com.coalesce.bot.`fun`.MarketplaceItem(marketplaceManager, image, title, price, System.currentTimeMillis(), context.author.idLong, 0)
        marketplaceManager[name] = meme
        context("Meme added.")
    }

    @com.coalesce.bot.command.SubCommand("Buy", "find shop", "Take a look at the marketplace.")
    @com.coalesce.bot.command.UserCooldown(10L, java.util.concurrent.TimeUnit.SECONDS)
    fun shop(context: com.coalesce.bot.command.CommandContext) {
        val user = marketplaceManager[context.author]
        val items = marketplaceManager.rawItemData.entries.toList().filter { !user.items.contains(it.value) }
        context(StringBuilder().apply {
            fun namer(it: Map.Entry<String, com.coalesce.bot.`fun`.MarketplaceItem>) = "${it.key} [${it.value.price}¢$] by ${context.main.jda.getUserById(it.value.owner)?.name}"
            fun basedOn(func: (Map.Entry<String, com.coalesce.bot.`fun`.MarketplaceItem>, Map.Entry<String, com.coalesce.bot.`fun`.MarketplaceItem>) -> Int) =
                append(items.order(func).subList(0, Math.min(items.size, 5)).joinToString(separator = "\n",
                        transform = ::namer))
            append("**Meme marketplace**\n")

            append("Recent additions:\n")
            basedOn { o1, o2 -> (o1.value.creation - o2.value.creation).toInt() }
            append("\nTop bought:\n")
            basedOn { o1, o2 -> o1.value.purchases - o2.value.purchases }
        }.toString())
    }

    @com.coalesce.bot.command.SubCommandAlias("Buy", "Search for a specific meme to buy.")
    fun query(context: com.coalesce.bot.command.CommandContext, query: String) {
        val item = marketplaceManager[query]
        if (item.owner == context.author.idLong) throw com.coalesce.bot.command.ArgsException("Can't buy your own meme.")
        if (marketplaceManager[context.author].items.contains(item)) throw com.coalesce.bot.command.ArgsException("You already have that meme.")
        val coins = context.main.coCoinsManager
        val bal = coins[context.author]
        if (item.price > bal.total) throw com.coalesce.bot.command.ArgsException("You can't afford that meme.")
        val owner = context.main.jda.getUserById(item.owner) ?: throw com.coalesce.bot.command.ArgsException("Seems like the creator of that meme abandoned the server.")

        coins[owner].transaction(com.coalesce.bot.CoCoinsTransaction("Your meme ${item.text} was purchased by ${context.author.name}.", item.price.toDouble()),
                context.channel, context.author)
        bal.transaction(com.coalesce.bot.CoCoinsTransaction("Purchased **${item.text}** meme", -item.price.toDouble()), context.channel, context.author)
        marketplaceManager[context.author].addItem(item, marketplaceManager, context.author)
        item.purchases ++
    }
}