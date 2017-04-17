package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@Command(name = "Whois", aliases = arrayOf("tellmeabout", "whos", "who's"), usage = "[user]", description = "Tells you about the user specified or yourself if none.", permission = "commands.whois")
class Whois : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (args.size > 1) {
            throw CommandError("Please follow the correct syntax: %s", annotation.usage)
        }
        val member: Member
        if (args.isEmpty()) {
            member = message.guild.getMember(message.author)
        } else {
            member = message.guild.getMember(message.mentionedUsers.stream().findFirst().orElseThrow { CommandError("Please specify a valid user.") })
        }
        val builder = EmbedBuilder().setColor(Color(0.0f, 0.5f, 0.0f)).setAuthor(member.user.name, null, member.user.avatarUrl)
                .addField("Nickname", member.nickname ?: "None", true)
                .addField("Discriminator", member.user.discriminator, true)
                .addField("User ID", member.user.id, true)
                .addField("Playing Currently", member.game?.name ?: "None", true)
                .addField("Roles", member.roles.stream().map { role -> "\u2666 " + role.name }.collect(Collectors.toList()).joinToString(separator = "\n"), true)
                .addField("Type", if (member.user.isBot) "Bot" else if (member.isOwner) "Owner" else "User", true)
                .addField("Creation Time", member.user.creationTime.format(DateTimeFormatter.ofPattern("d MMM uuuu")), true)
        if (member.roles.size == 1 && member.roles[0].name == "Python") {
            builder.addField("Has ugly yellow color?", "Sadly, yes", true)
        }
        message.channel.sendMessage(builder.build()).queue { it.delete().queueAfter(30, TimeUnit.SECONDS) }
    }
}
