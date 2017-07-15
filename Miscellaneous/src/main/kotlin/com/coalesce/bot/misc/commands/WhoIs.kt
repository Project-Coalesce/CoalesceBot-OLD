package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embTitle
import net.dv8tion.jda.core.entities.User
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Command("WhoIs", "whos whothefuckis checkthis")
class WhoIs: Embeddables {

    @CommandAlias("Retrieve more information about an user")
    fun execute(context: CommandContext, user: User) {
        val member = context.guild.getMember(user)
        context(embed().apply {
            embTitle = "Who is ${member.effectiveName}?"
            embColor = member.color
            setThumbnail(user.avatarUrl)
            field("Name", user.name, true)
            field("Discriminator", user.discriminator, true)
            field("ID", user.id, true)

            field("Join Date", member.joinDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            field("Creation Date", user.creationTime.format(DateTimeFormatter.ISO_LOCAL_DATE))

            field("Nickname", if (member.nickname != null) member.nickname else "No nickname.", true)
            field("Game", if (member.game != null) member.game.name else "No game.", true)

            field("Roles", member.roles.map{ "- ${it.name}" }.toList().joinToString(separator = "\n"), true)
        }, deleteAfter = 60L to TimeUnit.SECONDS)
    }
}