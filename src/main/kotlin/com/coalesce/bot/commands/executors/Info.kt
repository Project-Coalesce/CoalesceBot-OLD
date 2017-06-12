package com.coalesce.bot.commands.executors

import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.Embeddables
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import java.awt.Color

class Info: Embeddables {
    @RootCommand(
            name = "Info",
            permission = "commands.info",
            type = CommandType.INFORMATION,
            description = "Bot Information",
            aliases = arrayOf("moreinfo", "bot")
    )
    fun execute(context: RootCommandContext) {
        context(embed().apply {
            setColor(Color(255, 159, 50))

            setTitle("CoalesceBot", null)
            setDescription("Developed by the Coalesce Team (Learn more: `!coalesce`). Source: https://github.com/Project-Coalesce/CoalesceBot\n" +
                    "Any bugs should be reported to deprilula28#3609. Abusing commands may lead into a blacklist.\n" +
                    "Type `!help` for a list of commands.")
        }.build())
    }
}