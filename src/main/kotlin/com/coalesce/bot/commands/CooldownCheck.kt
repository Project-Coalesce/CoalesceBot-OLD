package com.coalesce.bot.commands

import com.coalesce.bot.utilities.formatTimeDiff
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.nio.file.Files.delete
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class CooldownCheck(val listener: Listener): Embeddables {
    fun cooldownCheck(it: CommandContext): Boolean {
        val cooldown: Double = if (it is SubCommandContext) {
            if (it.currentSubCommand.cooldown) {
                if (it.currentSubCommand.globalCooldown == 0.0) {
                    it.rootCommand.recursiveGlobalCooldown
                } else {
                    it.currentSubCommand.globalCooldown
                }
            } else {
                0.0
            }
        } else {
            it.rootCommand.globalCooldown
        }

        val userCooldowns = listener.userCooldowns[it.author.idLong] ?: mutableMapOf()
        val cooldownForCooldown = userCooldowns["cooldownMessage"] ?: 0L
        val identifier = if (it is SubCommandContext) "${it.rootCommand.name} ${it.currentSubCommand.name}" else it.rootCommand.name

        val setGlobal = cooldown != 0.0
        if (cooldown != 0.0) {
            val current = listener.cooldowns[identifier]
            if (current != null) {
                if (current > System.currentTimeMillis()) {
                    if (System.currentTimeMillis() > cooldownForCooldown) {
                        val remaining = (current.toLong() - System.currentTimeMillis())
                        it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl)
                                .setTitle("Global Cooldown for", null)
                                .setDescription(remaining.formatTimeDiff()), { delete().queueAfter(10L, TimeUnit.SECONDS) })
                        userCooldowns["cooldownMessage"] = System.currentTimeMillis() + 500
                    }
                    return false
                }
            }
        }

        // Global cooldown passed.
        val annoUser: Double = if (it is SubCommandContext) {
            if (it.currentSubCommand.cooldown) {
                if (it.currentSubCommand.userCooldown == 0.0) {
                    it.rootCommand.recursiveUserCooldown
                } else {
                    it.currentSubCommand.userCooldown
                }
            } else {
                0.0
            }
        } else {
            it.rootCommand.userCooldown
        }

        if (annoUser != 0.0) {
            val userCooldown = userCooldowns[identifier]
            if (userCooldown != null) {
                if (userCooldown > System.currentTimeMillis()) {
                    if (System.currentTimeMillis() > cooldownForCooldown) {
                        val remaining = (userCooldown.toLong() - System.currentTimeMillis())
                        it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl)
                                .setTitle("Cooldown for", null)
                                .setDescription(remaining.formatTimeDiff()), { delete().queueAfter(10L, TimeUnit.SECONDS) })
                        userCooldowns["cooldownMessage"] = System.currentTimeMillis() + 500
                    }
                    return false
                }
            }
            /*
            userCooldowns[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(annoUser.toLong())
            listener.userCooldowns[it.author.idLong] = userCooldowns
            */
        }
        /*
        if (setGlobal) {
            listener.cooldowns[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(cooldown.toLong())
        }
        */

        return true
    }

    fun setCooldown(context: CommandContext, user: User) {
        listener.apply {
            val name = if (context is SubCommandContext) "${context.rootCommand.name} ${context.currentSubCommand.name}" else context.rootCommand.name

            (userCooldowns[user.idLong] ?: run {
                userCooldowns[user.idLong] = mutableMapOf()
                userCooldowns[user.idLong]!!
            })[name] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis((if (context is SubCommandContext) context.currentSubCommand.userCooldown else context.rootCommand.userCooldown).toLong())
            cooldowns[name] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis((if (context is SubCommandContext) context.currentSubCommand.globalCooldown else context.rootCommand.globalCooldown).toLong())

        }
    }
}