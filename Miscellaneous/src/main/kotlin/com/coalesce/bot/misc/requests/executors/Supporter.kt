package com.coalesce.bot.misc.requests.executors

import com.coalesce.bot.command.ArgsException
import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.utilities.Embeddables
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.google.inject.Inject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Invite
import org.jsoup.Jsoup
import java.util.concurrent.ExecutorService

@Command("Supporter", "supporterrole getsupporterrole")
class Supporter @Inject constructor(val jda: JDA, val executorService: ExecutorService): Embeddables {
    private val webClient = WebClient(BrowserVersion.FIREFOX_3_6)
    private val supporterRole = jda.getRoleById(320952521965305858L)

    init {
        webClient.isThrowExceptionOnFailingStatusCode = false
    }

    @CommandAlias("Get the supporter role by inserting your profile URL")
    fun execute(context: CommandContext, url: String) {
        if (!url.startsWith("https://www.spigotmc.org/members/")) throw ArgsException("Please provide a Spigot profile URL.")
        executorService.submit {
            val page = webClient.getPage<HtmlPage>(url)
            webClient.waitForBackgroundJavaScript(10000)
            val jsoup = Jsoup.parse(page.asText())
            var found = false

            jsoup.select(".signature>[href]").forEach {
                val hrefURL = it.absUrl("href")
                if (hrefURL.startsWith("https://discord.gg/")) {
                    if (context.guild.idLong != Invite.resolve(context.main.jda, hrefURL.substring("https://discord.gg".length)).complete().guild.idLong) return@forEach
                    found = true
                    context("Thanks for supporting the guild! You have been awarded the supporter role. Don't remove the link from your signature as we will be " +
                            "checking it periodically!")
                    context.guild.controller.addRolesToMember(context.guild.getMember(context.author), supporterRole).queue()
                }
            }

            if (!found) context("No invites to this guild have been found in your signature. Type `!supporter` to learn more.")
        }
    }

    @CommandAlias("Get artwork for a supporter role to put in your signature")
    fun execute(context: CommandContext) {
        context("To get the supporter role, you need to add an invite to our discord in your spigot signature: https://discord.gg/bRZwvCC\n" +
                "If you don't know what text to put, you can use this artwork instead: http://i.imgur.com/0di0JI5.png" +
                "After you've set up a link, you can use `!supporter <profile url>`.")
    }
}
