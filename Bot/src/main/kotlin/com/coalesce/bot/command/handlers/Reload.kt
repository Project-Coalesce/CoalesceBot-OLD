package com.coalesce.bot.command.handlers

import com.coalesce.bot.GAMES
import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import java.util.concurrent.ThreadLocalRandom

@Command("Reload", "rl")
class Reload: Embeddables {
    @CommandAlias("Reloads all commands and plugins.")
    fun execute(context: CommandContext) {
        context.main.apply {
            @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // yay
            jda.presence.game = Game.of("reloading")
            jda.presence.status = OnlineStatus.DO_NOT_DISTURB

            context("Reloading...")
            loadCommands()
            context("Done.")

            @Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET") // ffs
            jda.presence.game = Game.of(GAMES[ThreadLocalRandom.current().nextInt(GAMES.size)])
            jda.presence.status = OnlineStatus.ONLINE
        }
    }
}

@Command("shutdown", "restart goodbye bye")
class Shutdown: Embeddables {
    @CommandAlias("Shuts down the JVM.")
    fun execute(context: CommandContext) = context("Goodbye, cruel world.") { System.exit(-1) }
}