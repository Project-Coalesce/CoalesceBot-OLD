package com.coalesce.bot.commands.executors

import com.coalesce.bot.VERSION
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
            aliases = arrayOf("moreinfo", "bot", "about")
    )
    fun execute(context: RootCommandContext) {
        context(embed().apply {
            setColor(Color(0xBE58B6))

            setAuthor("CoalesceBot v$VERSION", "https://github.com/Project-Coalesce/CoalesceBot", "http://i.imgur.com/4j7oFYD.png")
            setDescription(arrayOf(
                    "Developed by the Coalesce Team (Learn more: `!coalesce`).",
                    "If you find any bugs, please report them at https://goo.gl/5rGeFJ.",
                    "Abusing the bot's features may lead into a blacklist.",
                    "**Type `!help` for a list of commands.**")
                .joinToString(separator = "\n"))
        }.build())
    }
}