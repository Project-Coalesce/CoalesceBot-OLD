package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.gson.JsonElement
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.net.URL

@Command("UrbanDefinition", "ud udefine udefinition")
class UrbanDefinition @Inject constructor(val executorService: java.util.concurrent.ExecutorService): Embeddables {
    private val superscriptMap = mapOf('0' to "⁰", '1' to "¹", '2' to "²", '3' to "³", '4' to "⁴", '5' to "⁵", '6' to "⁶", '7' to "⁷", '8' to "⁸", '9' to "⁹")

    @CommandAlias("Defines something using the UrbanDictonary (rather *untrusted* source)")
    fun execute(context: CommandContext, @VarArg term: String) {
        context(embed().apply {
            embColor = Color.YELLOW
            embTitle = "Searching..."
        }) {
            executorService.submit {
                try {
                    val url = URL("http://api.urbandictionary.com/v0/define?term=${term.replace(" ", "+")}")
                    val json = gson.fromJson(url.openConnection().getInputStream().readText(), JsonElement::class.java).asJsonObject

                    if (json.get("result_type").asString == "no_results") {
                        editEmbed {
                            embTitle = "No definitions found!"
                            embColor = Color(206, 28, 28)
                        }
                        return@submit
                    }

                    val result = json["list"].asJsonArray.first().asJsonObject

                    editEmbed {
                        setTitle("Urban Dictionary Definition", result["permalink"].asString)
                        setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                        embColor = Color(112, 255, 45)

                        field("Term", term, false)
                        field("Result", result["definition"].asString.truncate(0, 1000), false)
                        field("Examples", result["example"].asString.truncate(0, 1000), false)

                        setFooter("By ${result["author"].asString} 👍${result["thumbs_up"].asInt} 👎${result["thumbs_down"].asInt}", null)
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