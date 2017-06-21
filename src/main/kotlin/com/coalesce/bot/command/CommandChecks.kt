package com.coalesce.bot.command

import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.formatTimeDiff
import com.coalesce.bot.utilities.timeOutHandler
import net.dv8tion.jda.core.entities.User
import java.util.concurrent.TimeUnit

class CooldownHandler: Embeddables {
    private val globalCooldown = mutableMapOf<CommandFrameworkClass.CommandInfo, Long>()
    private val userCooldown = mutableMapOf<User, MutableMap<CommandFrameworkClass.CommandInfo, Long>>()

    private fun doGlobalCooldown(info: CommandFrameworkClass.CommandInfo) {
        globalCooldown.put(info, System.currentTimeMillis())
        timeOutHandler(info.globalCooldown, TimeUnit.MILLISECONDS) { globalCooldown.remove(info) }
    }

    private fun doUserCooldown(user: User, info: CommandFrameworkClass.CommandInfo) {
        userCooldown[user] = (userCooldown[user] ?: mutableMapOf()).apply { put(info, System.currentTimeMillis()) }
        timeOutHandler(info.globalCooldown, TimeUnit.MILLISECONDS) { (userCooldown[user] ?: return@timeOutHandler).remove(info) }
    }

    fun cooldownCheck(context: CommandContext, info: CommandFrameworkClass.CommandInfo): Boolean {
        if (userCooldown.containsKey(context.author) && userCooldown[context.author]!!.containsKey(info)) {
            context(embed().apply {
                setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                setTitle("Wait before you can run this command again!", null)
                setDescription("⏰ Cooldown for: " + userCooldown[context.author]!![info]!!.formatTimeDiff())
            }.build(), deleteAfter = 8L to TimeUnit.SECONDS)
            return false
        }
        if (globalCooldown.containsKey(info)) {
            context(embed().apply {
                setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                setTitle("Wait before you can run this command again!", null)
                setDescription("⏰ Global Cooldown for: " + globalCooldown[info]!!.formatTimeDiff())
            }.build(), deleteAfter = 8L to TimeUnit.SECONDS)
            return false
        }

        return true
    }
}

class PermHandler: Embeddables {

}