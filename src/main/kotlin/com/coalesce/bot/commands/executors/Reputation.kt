package com.coalesce.bot.commands.executors

import com.coalesce.bot.ReputationTransaction
import com.coalesce.bot.ReputationValue
import com.coalesce.bot.commands.*
import com.coalesce.bot.gson
import com.coalesce.bot.reputationFile
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.*

class Reputation {
    @RootCommand(
            name = "reputation",
            type = CommandType.INFORMATION,
            permission = "commands.reputation",
            aliases = arrayOf("rep", "reput"),
            description = "View your reputation.",
            userCooldown = 10.0,
            globalCooldown = 3.0
    )
    fun execute(context: RootCommandContext) {
        val reputationStorage = HashMap<User, ReputationValue>()
        reputationFile.inputStream().use {
            it.reader().use {
                reputationStorage.putAll(gson.fromJson(it, reputationStorage::class.java))
            }
        }

        val rep = reputationStorage[context.message.author] ?: ReputationValue(0.0, mutableListOf<ReputationTransaction>())

        val transactionsString = StringBuilder()
        if (rep.transactions.isEmpty()) transactionsString.append("No transactions.")
        else rep.transactions.forEach {
            transactionsString.append("${if (it.amount >= 0) "+" else ""}${it.amount}: ${it.message}")
        }

        context.send(EmbedBuilder()
                .setTitle("You have ${rep.total} reputation.", null)
                .addField("Transactions", transactionsString.toString(), false))
    }

    @SubCommand(
            name = "thank",
            permission = "commands.reputation.thanks",
            aliases = arrayOf("thanks", "thankyou", "softdonate"),
            globalCooldown = 5.0,
            userCooldown = 360.0
    )
    fun thank(context: SubCommandContext) {
        if (context.message.mentionedUsers.isEmpty()) {
            context.send("You need to mention someone you wish to thank.")
            return
        }

        doThank(context.message.guild, context.message.channel, context.message.author, context.message.mentionedUsers.first())
    }

    fun doThank(guild: Guild, channel: MessageChannel, from: User, to: User) {
        if (from == to) {
            channel.sendMessage("You can't thank yourself.").queue()
            return
        }

        val reputationStorage = HashMap<User, ReputationValue>()
        reputationFile.inputStream().use {
            it.reader().use {
                reputationStorage.putAll(gson.fromJson(it, reputationStorage::class.java))
            }
        }

        val originValue = reputationStorage[from] ?: ReputationValue(0.0, mutableListOf<ReputationTransaction>())
        val targetValue = reputationStorage[to] ?: ReputationValue(0.0, mutableListOf<ReputationTransaction>())

        val transactionAmount = Math.min((originValue.total.toDouble() / 80.0) + 20.0, 100.0)
        targetValue.transaction(ReputationTransaction("${from.asMention} thanked you", transactionAmount),
                channel, guild.getMember(to))
        reputationStorage[to] = targetValue

        reputationFile.outputStream().use {
            it.writer(Charsets.UTF_8).write(gson.toJson(reputationStorage))
        }
    }
}