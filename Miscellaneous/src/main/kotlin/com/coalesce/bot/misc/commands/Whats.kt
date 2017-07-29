package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embTitle
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@Command("WhatIs", "whois whos whothefuckis checkthis whats whatthefuckis")
class WhoIs: Embeddables {
    @CommandAlias("Retrieve more information about a role")
    fun execute(context: CommandContext, role: Role) {
        context(embed().apply {
            embColor = role.color
            embTitle = "Role **${role.name}**"

            field("ID", role.id, true)
            field("Created At", SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(role.creationTime))
            field("Type", if (role.hasPermission(Permission.ADMINISTRATOR)) "Administrator"
                else if (role.hasPermission(Permission.MANAGE_CHANNEL)) "ChatMod" else "Common", true)
            field("Position", "Number ${role.position}", true)
            field("Members", "${role.guild.getMembersWithRoles(role).size} users")
        })
    }

    @CommandAlias("Retrieve more information about a channel")
    fun execute(context: CommandContext, channel: TextChannel) {
        context(embed().apply {
            embColor = Color.YELLOW
            embTitle = "Channel **#${channel.name}**"

            field("ID", channel.id, true)
            field("Topic", channel.topic, true)
            field("Created At", SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(channel.creationTime))
            field("Position", "Number ${channel.position}", true)
            field("Accessible by", if (channel.memberPermissionOverrides.isEmpty()) "Everyone" else "${channel.members.size} users")
        })
    }

    @CommandAlias("Retrieve more information about an user")
    fun execute(context: CommandContext, user: User = context.author) {
        val member = context.guild.getMember(user)
        context(embed().apply {
            embColor = member.color
            setThumbnail(user.avatarUrl)
            embTitle = "${user.name}#${user.discriminator}"

            if (member.nickname != null) field("Nickname", member.nickname, true)
            field("ID", user.id, true)

            field("Joined This Server At", SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(member.joinDate))
            field("Joined Discord At", SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(user.creationTime))

            if (member.game != null) field("Status", "${if (member.game.type.key == 0) "Playing " else "Streaming"} " +
                    member.game.name, true)

            field("Roles", member.roles.joinToString(separator = "\n") { "- ${it.name}" }, false)
        }, deleteAfter = 60L to TimeUnit.SECONDS)
    }
}