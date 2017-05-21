package com.coalesce.bot.commands

import java.awt.Color
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class CooldownCheck(val listener: Listener): Predicate<CommandContext>, Embeddables {
    override fun test(it: CommandContext): Boolean {
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

        val identifier = if (it is SubCommandContext) "${it.rootCommand.name} ${it.currentSubCommand.name}" else it.rootCommand.name

        var setGlobal: Boolean = false
        if (cooldown != 0.0) {
            val current = listener.cooldowns[identifier]
            if (current != null) {
                if (current > System.currentTimeMillis()) {
                    // TODO: Prettify current seconds
                    val remaining = (current.toLong() - System.currentTimeMillis())
                    it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl).setTitle("Cooldown", null)
                            .setDescription("That command is on global cooldown for ${prettify(remaining)}."))
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
            val user = listener.userCooldowns[it.author.idLong] ?: mutableMapOf() // All users should have one as long as it isnt empty.
            val userCooldown = user[identifier]
            if (userCooldown != null) {
                if (userCooldown > System.currentTimeMillis()) {
                    val remaining = (userCooldown.toLong() - System.currentTimeMillis())
                    it(embed().setColor(Color(204, 36, 24)).setAuthor(it.message.author.name, null, it.message.author.avatarUrl).setTitle("Cooldown", null)
                            .setDescription("That command is on cooldown for ${prettify(remaining)}."))
                    return false
                }
            }
            user[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(annoUser.toLong())
            listener.userCooldowns[it.author.idLong] = user
        }
        if (setGlobal) {
            listener.cooldowns[identifier] = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(cooldown.toLong())
        }

        return true
    }

    fun prettify(timeDiff: Long): String { //I just got this from an old project of mine, I'll prettify it later
        val second = timeDiff / 1000 % 60
        val minute = timeDiff / (1000 * 60) % 60
        val hour = timeDiff / (1000 * 60 * 60) % 24
        val day = timeDiff / (1000 * 60 * 60 * 24)

        if (day > 0) return "$day${ensurePlural(day, "day")} and $hour${ensurePlural(hour, "hour")}"
        if (hour > 0) return "$hour${ensurePlural(hour, "hour")} and $minute${ensurePlural(minute, "minute")}"
        if (minute > 0) return "$minute${ensurePlural(minute, "minute")} and $second${ensurePlural(second, "second")}"
        if (second > 0) return "$second${ensurePlural(second, "second")}"
        return timeDiff.toString() + "ms"
    }

    fun ensurePlural(numb: Long, str: String): String {
        return if (numb > 1) " ${str}s" else " $str"
    }
}