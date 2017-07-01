package com.coalesce.bot.misc.requests.executors

import com.coalesce.bot.Main
import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.misc.requests.Request
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import com.coalesce.bot.utilities.readText
import com.coalesce.bot.utilities.writeText
import com.google.inject.Inject
import com.sun.management.jmx.Trace.send
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Command("RoleRequest", "role getrole request")
class RoleRequest @Inject constructor(val bot: Main): Request {
    private val acceptableRoles = arrayOf(
            bot.jda.getRoleById("275473393325703179"), //Kotlin
            bot.jda.getRoleById("311314320006971393"), //Web
            bot.jda.getRoleById("299358012500606976"), //Spigot
            bot.jda.getRoleById("300819659404345346"), //Python
            bot.jda.getRoleById("275473304268177421"), //Java
            bot.jda.getRoleById("300377111728881664") //Project Coalesce
    )

    @CommandAlias("Request a role.")
    fun request(context: CommandContext, role: Role) {
        val member = context.guild.getMember(context.author)
        if (!acceptableRoles.contains(role) || member.roles.contains(role)) throw ArgsException("You need to provide one of: "
            + acceptableRoles.filter { !member.roles.contains(role) }.joinToString(separator = ", ") { it.name })

        context.usePCh {
            send(embed().apply {
                setTitle("More information on your role request (${role.name})", "https://github.com/login/oauth/authorize?client_id=2e8a8ed194265736c99a&state" +
                        "=${role.id}+${user.id}")
                embDescription =
                        "We need to verify your ability to code in the respective language by looking at your GitHub profile." +
                        "\nIt's also needed to make sure the GitHub account belongs to you, and so we request a one-time authentication" +
                        "(Click on the title of this box).\n" +
                        "You can remove the authorization at any time in your GitHub profile."
            }.build())
            context("Your request for the role ${role.name} is almost ready, but we need to verify your GitHub profile first. " +
                    "Look on your DM's for more information.")
        }
    }

    @JDAListener
    fun react(event: GuildMessageReactionAddEvent) = uponReact(event, "role")

    @CommandAlias("Internal command, ignore..")
    fun finished(context: CommandContext, code: String, user: String, role: String) {
        if (!context.author.isBot || context.channel.idLong != 299385639437074433L) throw ArgsException("I meant it when I said \"ignore\"..")
        val resolvedUser = context.main.jda.getUserById(user) ?: return
        val resolvedRole = context.main.jda.getRoleById(role) ?: return

        val htmlURL = verifyAuthenticationTokenGithub(code)
        createRequest(context, resolvedUser) {
            setTitle("Requested for the role ${resolvedRole.name}", htmlURL)
            embDescription = "Click on the title to view their github profile."
            setFooter("role${resolvedUser.idLong},${resolvedRole.idLong},${context.guild.idLong}", null)
        }
    }

    private fun verifyAuthenticationTokenGithub(code: String): String {
        val url = URL("https://github.com/login/oauth/access_token")
        val userURL = "https://api.github.com/user"

        val accessToken = post(url, "client_id=2e8a8ed194265736c99a&client_secret=${bot.githubSecret}&code=$code")
                .split("access_token=")[1].split("&")[0]
        val userInfo = get(URL("$userURL?access_token=$accessToken"))

        val json = mutableMapOf<String, Any?>()
        json.putAll(gson.fromJson(userInfo, json::class.java))

        if (json.containsKey("error")) throw IOException(json["error_description"] as String)
        return json["html_url"] as String
    }

    private fun post(url: URL, output: String): String {
        val conn = url.openConnection() as HttpURLConnection
        conn.doOutput = true
        conn.requestMethod = "POST"
        conn.outputStream.use {
            it.writeText(output)
            it.flush()
        }
        return conn.inputStream.readText()
    }

    private fun get(url: URL): String = url.openConnection().getInputStream().readText()

    override fun response(message: String, embed: MessageEmbed, accepted: Boolean) {
        val split = message.split(",")
        val user = bot.jda.getUserById(split[0])
        val role = bot.jda.getRoleById(split[1])
        val guild = bot.jda.getGuildById(split[2])
        if (accepted) {
            guild.controller.addRolesToMember(guild.getMember(user), role).queue {
                user.usePCh {
                    send("You have been awarded the ${role.name} role on ${guild.name}!")
                }
            }
        } else {
            user.usePCh {
                send("Your request for the role ${role.name} on the server ${guild.name} was rejected by moderators.\n" +
                     "Make sure you have code of the respective language in your GitHub account.")
            }
        }
    }
}