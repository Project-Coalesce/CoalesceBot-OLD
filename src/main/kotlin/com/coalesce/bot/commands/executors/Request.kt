package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.JDAListener
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.gson
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.PrivateChannel
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.URL
import java.util.*

class Request(@Inject val bot: Main) {
    private val acceptableRoles = arrayOf(
            bot.jda.getRoleById("275473393325703179"), //Kotlin
            bot.jda.getRoleById("311314320006971393"), //Web
            bot.jda.getRoleById("299358012500606976"), //Spigot
            bot.jda.getRoleById("300819659404345346"), //Python
            bot.jda.getRoleById("275473304268177421"), //Java
            bot.jda.getRoleById("300377111728881664") //Project Coalesce
    )

    @RootCommand(
            name = "request",
            aliases = arrayOf("role", "getrole", "requestrole"),
            globalCooldown = 0.0,
            type = CommandType.INFORMATION,
            permission = "commands.roleRequest",
            description = "Request for a developer role."
    )
    fun execute(context: RootCommandContext) {
        if (context.args.isEmpty()) {
            context("You need to provide a role!")
            return
        }

        val role : Role

        if (context.message.mentionedRoles.isNotEmpty()) {
            role = context.message.mentionedRoles.first()
        } else {
            val roles = bot.jda.getRolesByName(context.args.first(), true)

            if (roles.isEmpty()) {
                context("Role not found.")
                return
            }

            role = roles.first()
        }

        if (!acceptableRoles.contains(role)) {
            context("You can't request that role.")
            return
        }

        if (context.message.guild.getMember(context.message.author).roles.contains(role)) {
            context("You can't ask for a role you already have.")
            return
        }

        if (context.message.author.hasPrivateChannel()) sendMessage(context.message.author.privateChannel, context.channel, role, context.message.author)
        else context.message.author.openPrivateChannel().queue { sendMessage(it, context.channel, role, context.message.author) }
    }

    fun sendMessage(pmChannel: PrivateChannel, globalChannel: MessageChannel, role: Role, user: User) {
        pmChannel.sendMessage(EmbedBuilder()
                .setTitle("You requested for the '" + role.name + "' developer role.", null)
                .setDescription("We need to verify your ability to code in the respective language by looking at your GitHub profile." +
                        "\nIt's also needed to make sure the GitHub account belongs to you, and so we request a one-time authentication" +
                        "with the link provided below. You can remove the authorization at any time in your profile." +
                        "\nWarning: You have to verify within 10 minutes.")
                .addField("Authorize your GitHub Account",
                        "https://github.com/login/oauth/authorize?client_id=2e8a8ed194265736c99a&scope=user&state" +
                                "=${role.id},${user.id}", false)
                .build()).queue()

        globalChannel.sendMessage(user.asMention + " I've sent you a private message with instructions.").queue()
    }
}

class ValidateRequest(@Inject val bot: Main) {
    private val acceptableRoles = arrayOf(
            bot.jda.getRoleById("275473393325703179"), //Kotlin
            bot.jda.getRoleById("311314320006971393"), //Web
            bot.jda.getRoleById("299358012500606976"), //Spigot
            bot.jda.getRoleById("300819659404345346"), //Python
            bot.jda.getRoleById("275473304268177421"), //Java
            bot.jda.getRoleById("300377111728881664") //Project Coalesce
    )
    private val tagRequests = bot.jda.getTextChannelById("311317585775951872")

    @RootCommand(
            name = "validaterequests",
            aliases = arrayOf("validaterequest"),
            globalCooldown = 0.0,
            type = CommandType.ADMINISTRATION,
            permission = "commands.validateRequests",
            description = "Command you shouldn't do."
    )
    fun execute(context: RootCommandContext) {
        if (context.channel.id != "299385639437074433") {
            context("I really meant it when I wrote 'shouldn't do' in the description..")
            return
        }

        val state = context.args[0].split(",")
        val code = context.args[1]

        val role = context.jda.getRoleById(state[1])
        val user = context.jda.getUserById(state[2])
        val channel = user.privateChannel

        if (!acceptableRoles.contains(role)) {
            channel.sendMessage("Nice try " + user.asMention).queue()
            return
        }

        //GitHub verification
        try {
            val htmlURL = verifyAuthenticationTokenGithub(code)
            tagRequests.sendMessage(EmbedBuilder()
                    .setAuthor(user.name, htmlURL, user.avatarUrl?: "https://cdn.discordapp.com/attachments/300377073678286848/313174922677452804/theme_image_22.png")
                    .setTitle("Requested " + role.name, null)
                    .setDescription("Click on the author for github URL.")
                    .setFooter("${user.id} ${role.id}", null)
                    .build()).queue {
                it.addReaction("✅").queue()
                it.addReaction("❎").queue()
                user.privateChannel.sendMessage("Your application for the ${role.name} role on Coalesce Coding " +
                        "was sent to the administration team.")
            }
        } catch (e: IOException) {
            channel.sendMessage(e.message).queue()
        }


    }

    @JDAListener
    fun onReact(event: MessageReactionAddEvent) {
        if (event.channel == tagRequests) {
            val accepted : Boolean
            if (event.reaction.emote.name == "✅") accepted = true
            else if (event.reaction.emote.name == "❎") accepted = false
            else return

            event.channel.getMessageById(event.messageId).queue {
                val footText = it.embeds.first().footer.text.split(" ")
                val user = event.guild.getMember(event.jda.getUserById(footText[0]))
                val role = event.jda.getRoleById(footText[1])

                if (accepted) {
                    event.guild.controller.addRolesToMember(user, role).queue {
                        event.channel.sendMessage("The '${role.name}' role was assigned to ${user.effectiveName}.").queue()
                        user.user.privateChannel.sendMessage("Your application for the ${role.name} role on Coalesce Coding " +
                                "has been responded positively, and you have been assigned it.")
                    }
                } else {
                    event.channel.sendMessage("The '${role.name}' role was rejected for ${user.effectiveName}.").queue()
                    user.user.privateChannel.sendMessage("Your application for the ${role.name} role on Coalesce Coding " +
                            "has been responded negatively, and thus you won't get it.\nMake sure you have code in the " +
                            "respective language on your GitHub account.")
                }
            }
        }
    }

    fun verifyAuthenticationTokenGithub(code: String) : String {
        val url = URL("https://github.com/login/oauth/access_token")
        val userURL = URL("https://api.github.com/user")

        val accessToken = post(url, "client_id=2e8a8ed194265736c99a&client_secret=${bot.githubSecret}&code=$code")
                .split("access_token=")[1].split("&")[0]
        val userInfo = post(userURL, "{\"access_token\"=\"$accessToken\"}")

        val json = mutableMapOf<String, Any?>()
        json.putAll(gson.fromJson(userInfo, json::class.java))

        if (json.containsKey("error")) throw IOException(json["error_description"] as String)
        return json["html_url"] as String
    }

    fun post(url: URL, output: String): String {
        val conn = url.openConnection()

        conn.doOutput = true
        val writer = OutputStreamWriter(conn.getOutputStream())
        writer.write(output)
        writer.close()

        val scanner = Scanner(conn.getInputStream())
        val string = scanner.nextLine()
        scanner.close()

        return string
    }
}