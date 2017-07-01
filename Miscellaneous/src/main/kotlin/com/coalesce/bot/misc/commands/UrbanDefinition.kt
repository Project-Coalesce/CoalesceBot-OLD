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
                        editMessage(EmbedBuilder(embeds.first()).apply {
                            embTitle = "No definitions found!"
                            embColor = Color(206, 28, 28)
                        }.build()).queue()
                        return@submit
                    }

                    val result = json["list"].asJsonArray.first().asJsonObject
                    val definition = result["definition"].asString
                    val references = definition.matchList(Regex("\\[.*?\\]"))

                    references.forEachIndexed { index, it ->
                        definition.replaceRange(it.range.start + 1 .. it.range.endInclusive - 1, it.value.substring(1, it.value.length - 1) +
                            it.value.toCharArray().map { superscriptMap[it] })
                    }
                    editMessage(EmbedBuilder(embeds.first()).apply {
                        setTitle("Urban Dictionary Definition", result["permalink"].asString)
                        setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                        embColor = Color(112, 255, 45)

                        field("Term", term, false)
                        field("Result", definition.truncate(0, 1000) + "\n\n**Examples:**\n\n" +
                            "*${result["example"].asString.truncate(0, 500)}*", false)
                        if (references.isNotEmpty()) {
                            val maxSize = references.size / 1024 - references.size * 7 /* New lines, number. etc... */
                            field("References", references.joinToString(separator = "\n") {
                                val refResult = gson.fromJson(URL("http://api.urbandictionary.com/v0/define?term=${it.value}")
                                        .openConnection().getInputStream().readText(), JsonElement::class.java)
                                        .asJsonObject["list"].asJsonArray.first().asJsonObject

                                "${references.indexOf(it)}." + refResult["definition"].asString.truncate(0, maxSize)
                            }, false)
                        }
                        field("Ratings", "👍${result["thumbs_up"].asInt} 👎${result["thumbs_down"].asInt}", true)
                        field("By", result["author"].asString, true)
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