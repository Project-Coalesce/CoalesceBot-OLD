package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embDescription
import com.coalesce.bot.utilities.embTitle
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.http.client.utils.URIBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.util.concurrent.ExecutorService

@Command("Google", "search")
class Google @Inject constructor(val executorService: ExecutorService): Embeddables {

    @UserCooldown(5L)
    @CommandAlias("Google something")
    fun google(context: CommandContext, @VarArg query: String) {
        context(embed().apply {
            embColor = Color.CYAN
            embTitle = "Searching..."
        }) {
            executorService.submit {
                val url = URIBuilder("https://www.google.com/search").addParameter("q", query.replace(" ", "+")).build().toString()

                val sections = Jsoup.connect(url).userAgent("Mozilla/5.0").get().select(".g")

                if (sections.isEmpty()) {
                    editMessage(EmbedBuilder(embeds.first()).apply {
                        embColor = Color.RED
                        embTitle = "No results!"
                        embDescription = "Nothing found for $query"
                    }.build()).queue()
                    return@submit
                }

                editMessage(EmbedBuilder(embeds.first()).apply {
                    embTitle = null
                    setAuthor("Google", url, "http://i.imgur.com/YE6Agjf.png")
                    embColor = Color.GREEN
                    var count = 0
                    for (section in sections) {
                        if (count >= 5) break

                        val list = section.select(".r>a")

                        if (list.isEmpty()) return@submit

                        val entry = list.first()
                        val title = entry.text()
                        val url = entry.absUrl("href").replace(")", "\\)")
                        var description: String? = null

                        val fetch = section.select(".st")
                        if (!fetch.isEmpty()) description = fetch.first().text()

                        addField("**$title** ($url)", description, false)

                        ++count
                    }
                }.build()).queue()
            }
        }
    }
}