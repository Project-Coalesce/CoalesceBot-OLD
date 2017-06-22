package com.coalesce.bot.command.handlers

import com.coalesce.bot.command.*
import com.coalesce.bot.commandPrefix
import com.coalesce.bot.utilities.*
import java.awt.Color
import java.awt.SystemColor.info
import java.util.concurrent.TimeUnit
import kotlin.reflect.KParameter

@Command("Help", "? hlp")
@UserCooldown(30L)
class Help: Embeddables {
    companion object {
        private val pages = mutableListOf<String>()

        fun loadHelp(handler: Listener) {
            var builder = StringBuilder()

            fun addLine(str: String) {
                if (builder.length + str.length > 1024) {
                    pages.add(builder.toString())
                    builder = StringBuilder()
                }
                builder.appendln(str)
            }

            handler.commands.forEach {
                val alias = it.commandInfo.aliases[0]
                fun name(method: Pair<List<Class<*>>, List<KParameter>>) =
                        "${commandPrefix}${alias.toLowerCase()} " + method.second.subList(1).filter {
                            it.type.classifier != CommandContext::class }.joinToString(separator = " ") { if (it.isOptional) "[${it.name!!.capitalize()}]" else "<${it.name!!.capitalize()}>" }

                it.botCommand.methods.forEach {
                    val usageString = name(it.key)
                    val desc = it.value.second
                    addLine("`$usageString`: $desc")
                }
                it.botCommand.subCommands.forEach {
                    it.value.methods.forEach {
                        val usageString = name(it.key)
                        val desc = it.value.second
                        addLine("`$usageString`: $desc")
                    }
                }
            }

            pages.add(builder.toString())
        }
    }

    @CommandAlias("Gives you a list of commands.")
    fun execute(context: CommandContext, page: Int = 1) {
        println(pages.size)
        if (page !in 1..pages.size) throw ArgsException("Invalid page!")

        context.usePCh {
            send(embed().apply {
                embTitle = "Command List Page $page"
                embColor = Color(0xBE58B6)
                embDescription = pages[page - 1]
            }, context.author)
            context("I've sent you a list of commands in your private messages!", deleteAfter = 10L to TimeUnit.SECONDS)
        }
    }
}