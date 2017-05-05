package com.coalesce.bot.commands.executors

import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.utilities.ifwithDo
import java.util.concurrent.TimeUnit

class EggsDee {
    @RootCommand(
            name = "xD",
            aliases = arrayOf("bramsblessing", "eggsdee", "eggsd"),
            description = "Meme command (Eggs dee, boi.)",
            permission = "commands.eggsdee",
            globalCooldown = 30.0,
            type = CommandType.FUN
    )
    fun execute(context: RootCommandContext) {
        context.send(":egg: :egg: :regional_indicator_d: :regional_indicator_e: :regional_indicator_e:") { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(20, TimeUnit.SECONDS) } }
    }
}