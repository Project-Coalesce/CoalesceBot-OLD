package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.utilities.*
import com.google.inject.Inject
import org.jsoup.Jsoup
import java.awt.Color
import java.util.concurrent.ExecutorService

@Command("CyanideAndHappiness", "rcah randomcomic comic cah")
class CyanideAndHappiness @Inject constructor(val executorService: ExecutorService): Embeddables  {

    @CommandAlias("Cyanide and Happiness random comic")
    @UserCooldown(20L)
    @GlobalCooldown(10L)
    fun execute(context: CommandContext) {
        context(embed().apply {
            embColor = Color.YELLOW
            setTitle("Cyanide and Happiness", "http://explosm.net/")
            embDescription = "Loading..."
        }) {
            executorService.submit {
                try {
                    val url = Jsoup.connect("http://explosm.net/rcg").get()
                            .getElementById("rcg-comic")
                            .getElementsByTag("img")
                            .first()
                            .absUrl("src")
                    editEmbed {
                        embDescription = "**Random Comic**"
                        setImage(url)
                        setThumbnail("http://explosm.net/img/logo.png")
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