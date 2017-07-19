package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.misc.userAgent
import com.coalesce.bot.utilities.Embeddables
import com.coalesce.bot.utilities.description
import com.coalesce.bot.utilities.embColor
import com.coalesce.bot.utilities.embTitle
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
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
                    val url = "www.google.com/search?q=${query.replace(" ", "+")}&glp=1&hl=EN"
                    val jsoup = Jsoup.connect(url).userAgent(userAgent).get()
                    val sections = jsoup.select(".g")

                    if (sections.isEmpty()) {
                        editMessage(EmbedBuilder(embeds.first()).apply {
                            embTitle = "No results found!"
                            embColor = Color(206, 28, 28)
                        }.build()).queue()
                        return@submit
                    }

                    editEmbed {
                        embColor = Color(112, 255, 45)
                        embTitle = null
                        setAuthor("Google", url, "http://i.imgur.com/YE6Agjf.png")
                        val infoObjects = jsoup.select("._OKe")
                        if (infoObjects.isNotEmpty()) {
                            val obj = infoObjects.first()
                            description {
                                appendln("**${obj.select("._Q1n").joinToString(separator = "\n") { it.select("span").text() }}**")
                                append(obj.select("._RBg>.mod").select("span").joinToString(separator = "\n") { it.text() })
                            }
                        }
                        var count = 0
                        sections.subList(0, Math.max(sections.size, 5)).forEach {
                            val list = it.select(".r>a")
                            if (list.isEmpty()) return@editEmbed

                            val entry = list.first()
                            val title = entry.text()
                            val linkURL = entry.absUrl("href").replace(")", "\\)")

                            val fetch = it.select(".st")
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
                            appendln("This has been reported to Coalesce developers.")
                        }
                    }
                    ex.printStackTrace()
                }
            }
        }
    }
}