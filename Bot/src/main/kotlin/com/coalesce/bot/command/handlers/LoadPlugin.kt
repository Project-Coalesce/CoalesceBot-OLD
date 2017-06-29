package com.coalesce.bot.command.handlers

import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.pluginsFolder
import com.coalesce.bot.utilities.Embeddables
import java.io.File

@Command("LoadPlugin", "addplugin plugin")
class LoadPlugin: Embeddables {
    @CommandAlias("Adds a plugin")
    fun execute(context: CommandContext, name: String) {
        if (context.message.attachments.isEmpty()) throw ArgsException("You need to attach a plugin JAR file!")
        val attach = context.message.attachments.first()
        context("Downloading...")
        attach.download(File(pluginsFolder, "$name.jar"))
        context("Plugin added! You can reload with `!reload`.")
    }
}