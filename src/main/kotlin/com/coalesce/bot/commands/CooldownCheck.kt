package com.coalesce.bot.commands

import com.coalesce.bot.utilities.formatTimeDiff
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

        var setGlobal: Boolean = false
        if (cooldown != 0.0) {
            val current = listener.cooldowns[identifier]
            if (current != null) {
                if (current > System.currentTimeMillis() && System.currentTimeMillis() > cooldownForCooldown) {
                    val remaining = (current.toLong() - System.currentTimeMillis())
                    it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl)
                            .setTitle("Global Cooldown for", null)
                            .setDescription(remaining.formatTimeDiff()), { delete().queueAfter(10L, TimeUnit.SECONDS) })
                    userCooldowns["cooldownMessage"] = System.currentTimeMillis() + 500
                    return false
                }
            }
            setGlobal = true
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
                if (userCooldown > System.currentTimeMillis() && System.currentTimeMillis() > cooldownForCooldown) {
                    val remaining = (userCooldown.toLong() - System.currentTimeMillis())
                    it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl)
                            .setTitle("Cooldown for", null)
                            .setDescription(remaining.formatTimeDiff()), { delete().queueAfter(10L, TimeUnit.SECONDS) })
                    userCooldowns["cooldownMessage"] = System.currentTimeMillis() + 500
                    return false
                }
            }
            userCooldowns[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(annoUser.toLong())
            listener.userCooldowns[it.author.idLong] = userCooldowns
        }
        if (setGlobal) {
            listener.cooldowns[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(cooldown.toLong())
        }

        return true
    }
}