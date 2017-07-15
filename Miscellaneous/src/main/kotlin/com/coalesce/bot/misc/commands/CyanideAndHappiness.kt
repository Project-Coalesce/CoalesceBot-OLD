package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.editEmbed
import com.coalesce.bot.utilities.*
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.util.concurrent.ExecutorService

@Command("CyanideAndHappiness", "rcah randomcomic comic cah")
class CyanideAndHappiness @Inject constructor(val executorService: ExecutorService): Embeddables  {

    @CommandAlias("Cyanide and Happiness random comic")
    fun execute(context: CommandContext) {
        context(embed().apply {
            embTitle = "Cyanide and Happiness"
            embDescription = "Loading..."
            embColor = Color.YELLOW
        }) {
            executorService.submit {
                try {
                    val url = Jsoup.connect("http://explosm.net/rcg").get()
                            .getElementById("rcg-comic")
                            .getElementsByTag("img")
                            .first()
                            .absUrl("src")
                    editMessage(EmbedBuilder(embeds.first()).apply {
                        embDescription = "**Random Comic**"
                        setImage(url)
                        setThumbnail("http://explosm.net/img/logo.png")
                    }.build()).queue()
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