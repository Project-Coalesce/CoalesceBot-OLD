package com.coalesce.bot.administration.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.VarArg
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Game

@Command("Playing")
class Playing @Inject constructor(val jda: JDA) {

    @CommandAlias("Change the bot status")
    fun execute(context: CommandContext, @VarArg status: String) {
        context("Now Playing **$status**") {
            jda.presence.game = Game.of(status)
        }
    }
}