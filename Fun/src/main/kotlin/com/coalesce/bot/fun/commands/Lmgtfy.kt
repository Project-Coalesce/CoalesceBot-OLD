package com.coalesce.bot.`fun`.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.VarArg
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import net.dv8tion.jda.core.entities.User
import java.awt.Color

@Command("lmgtfy", "lemmegoog justgoogleretard badcommand")
class Lmgtfy: Embeddables {
    @CommandAlias("Have you tried googling it? Let me do it for you!")
    fun lmgtfy(context: CommandContext, mention: User = context.author, @VarArg query: String) {
        context(embed().apply {
            setAuthor(mention.name, null, mention.effectiveAvatarUrl)
            setTitle("Have you tried googling it?", "http://lmgtfy.com/?q=" + query.replace(" ", "+"))
            embColor = Color.ORANGE
            embDescription = "Click the title for more info"
        })
    }
}