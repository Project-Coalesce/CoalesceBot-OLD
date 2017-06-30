package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.Command
import com.coalesce.bot.command.CommandAlias
import com.coalesce.bot.command.CommandContext
import com.coalesce.bot.command.VarArg
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.gson.JsonElement
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.net.URL

@Command("UrbanDefinition", "ud udefine udefinition")
class UrbanDefinition @Inject constructor(val executorService: java.util.concurrent.ExecutorService): Embeddables {
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
                        editMessage(EmbedBuilder(embeds.first()).apply {
                            embTitle = "No definitions found!"
                            embColor = Color(206, 28, 28)
                        }.build()).queue()
                        return@submit
                    }

                    val firstResult = json.get("list").asJsonArray.get(0).asJsonObject
                    editMessage(EmbedBuilder(embeds.first()).apply {
                        setTitle("Urban Dictionary Definition", firstResult.get("permalink").asString)
                        setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                        embColor = Color(112, 255, 45)

                        field("Term", term, true)
                        field("Result", firstResult.get("definition").asString.truncate(0, 1000), true)
                        field("Example", firstResult.get("example").asString.truncate(0, 1000), true)
                        field("Ratings", "👍${firstResult.get("thumbs_up").asInt} 👎${firstResult.get("thumbs_down").asInt}", false)
                    }.build()).queue()
                } catch (ex: Exception) {
                    editMessage(EmbedBuilder(embeds.first()).apply {
                        embTitle = "Error"
                        embColor = Color(232, 46, 0)

                        description {
                            appendln("Failed to provide results!")
                            appendln("${ex.javaClass.name}: ${ex.message}")
                            appendln("This has been reported to coalesce developers.")
                        }
                    }.build()).queue()
                    ex.printStackTrace()
                }
            }
        }
    }
}