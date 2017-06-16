package com.coalesce.bot.commands.executors

import com.coalesce.bot.Colour
import com.coalesce.bot.canDelete
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.ifwithDo
import net.dv8tion.jda.core.entities.Member
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class Whois : Embeddables {
    @RootCommand(
            name = "Whois",
            aliases = arrayOf("tellmeabout", "whos", "who's", "newlyfwhodis"),
            description = "Tells you about the user specified or yourself if none.",
            permission = "commands.whois",
            type = CommandType.INFORMATION,
            globalCooldown = 10.0
    )
    fun execute(context: RootCommandContext) {
        val member: Member?
        if (context.args.isEmpty()) {
            member = context.message.guild.getMember(context.author)
        } else {
            member = context.message.guild.getMember(context.message.mentionedUsers.firstOrNull())
        }
        if (member == null) {
            throw ArgsException("You must mention an user to view information of.")
        }
        val builder = embed()
                .data(null, colour = Colour.GREEN, author = member.user.name, avatar = member.user.avatarUrl)
                .field("Nickname", member.nickname ?: "None", true)
                .field("Discriminator", member.user.discriminator, true)
                .field("User ID", member.user.id, true)
                .field("Playing", member.game?.name ?: "Nothing", true)
                .field("Roles", member.roles.map { "\u2666 ${it.name}" }.toList().joinToString(separator = "\n"), true)
                .field("Type", if (member.user.isBot) "Bot" else if (member.isOwner) "Guild Owner" else "User")
                .field("Creation Time", member.user.creationTime.format(DateTimeFormatter.ofPattern("d MMM uuuu")), true)
        if (member.roles.size == 1 && member.roles[0].name == "Python") {
            builder.addField("Has ugly yellow color?", "Sadly, yes", true)
        }
        context(builder) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(60, TimeUnit.SECONDS) } }
    }
}