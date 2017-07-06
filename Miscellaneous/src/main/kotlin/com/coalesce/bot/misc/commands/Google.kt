package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.misc.AGENT
import com.coalesce.bot.utilities.*
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.http.client.utils.URIBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.util.concurrent.ExecutorService

@UserCooldown(5L)
@Command("Google", "goog gog search")
class Google @Inject constructor(val executorService: ExecutorService): Embeddables {
    @CommandAlias("Google something")
    fun google(context: CommandContext, @VarArg query: String) {
        context(embed().apply {
            embColor = Color.YELLOW
            embTitle = "Searching..."
        }) {
            executorService.submit {
                try {
                    val url = URIBuilder("https://www.google.com/search").addParameter("q", query.replace(" ", "+")).build().toString()
                    val sections = Jsoup.connect(url).userAgent(AGENT).get().select(".g")

                    if (sections.isEmpty()) {
                        editMessage(EmbedBuilder(embeds.first()).apply {
                            embTitle = "No results found!"
                            embColor = Color(206, 28, 28)
                        }.build()).queue()
                        return@submit
                    }

                    editEmbed {
                        embTitle = null
                        setAuthor("Google", url, "http://i.imgur.com/YE6Agjf.png")
                        embColor = Color(112, 255, 45)
                        var count = 0
                        for (section in sections) {
                            if (count >= 5) break
                            val list = section.select(".r>a")
                            if (list.isEmpty()) return@editEmbed

                            val entry = list.first()
                            val title = entry.text()
                            val linkURL = entry.absUrl("href").replace(")", "\\)")

                            val fetch = section.select(".st")
                            val description = if (!fetch.isEmpty()) fetch.first().text() else "*No description*"

                            addField("**$title** ($linkURL)", description, false)

                            ++count
                        }
                    }
                } catch (ex: Exception) {
                    editEmbed {
                        embTitle = "Error"
                        embColor = Color(232, 46, 0)

                        description {
                            appendln("Failed to provide results!")
                            appendln("${ex.javaClass.name}: ${ex.message}")
                            appendln("This has been reported to coalesce developers.")
                        }
                    }
                    ex.printStackTrace()
                }
            }
        }
    }
}