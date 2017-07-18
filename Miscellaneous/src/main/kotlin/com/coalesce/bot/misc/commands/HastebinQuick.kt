package com.coalesce.bot.misc.commands

import com.coalesce.bot.AGENT
import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.gson.JsonElement
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Message
import java.awt.Color
import java.net.HttpURLConnection
import java.net.URL

@Command("HastebinQuick", "hb hastebin pastebin pb codeblock")
class HastebinQuick @Inject constructor(val executorService: java.util.concurrent.ExecutorService): Embeddables {
    private val codeBlock = Regex("```[\\s\\S]*```")
    private val id: String

    init {
        val url = URL("https://hastebin.com")
        val conn = url.openConnection()
        conn.addRequestProperty("User-Agent", AGENT)
        conn.getInputStream().readText() // Ensure connection
        val cookies = conn.headerFields["Set-Cookie"]!!
        id = cookies.find { it.startsWith("__cfduid=") }!!.split(";").first()
    }

    @CommandAlias("Creates a hastebin with the provided code")
    fun execute(context: CommandContext, @VarArg code: String) {
        context(embed().apply {
            embColor = Color.YELLOW
            embTitle = "Posting..."
        }) {
            executorService.submit {
                createHastebin(context, code, this)
            }
        }
    }

    @CommandAlias("Creates hastebin with previous messages")
    fun previousMessages(context: CommandContext) {
        context.channel.history.retrievePast(10).queue {
            val message = it.firstOrNull { it.rawContent.contains(codeBlock) } ?: throw ArgsException("No messages found!\nYou can use !hastebin <code> instead.")
            message.delete().queue()
            context(embed().apply {
                embColor = Color.YELLOW
                embTitle = "Posting..."
            }) {
                executorService.submit {
                    createHastebin(context, message.rawContent.matching(codeBlock).let { it.substring(3 .. it.length - 3) }, this)
                }
            }
        }
    }

    private val properties: Map<String, String>
        get() = mapOf(
                "Cookie" to id,
                "User-Agent" to AGENT,
                "Content-Type" to "application/json; charset=utf-8"
        )

    private fun createHastebin(context: Context, code: String, message: Message) {
        try {
            val url = URL("https://hastebin.com/documents")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            properties.forEach { conn.addRequestProperty(it.key, it.value) }
            conn.doOutput = true
            conn.outputStream.use {
                it.writeText(code)
                it.flush()
            }
            val json = gson.fromJson(conn.inputStream.readText(), JsonElement::class.java).asJsonObject

            message.editEmbed {
                setTitle("Hastebin Link Generated", "https://hastebin.com/" + json["key"].asString)
                setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                embColor = Color(112, 255, 45)
                embDescription = "Click the title to view!"
            }
        } catch (ex: Exception) {
            message.editEmbed {
                embTitle = "Error"
                embColor = Color(232, 46, 0)

                description {
                    appendln("Failed to send request!")
                    appendln("${ex.javaClass.name}: ${ex.message}")
                    appendln("This has been reported to coalesce developers.")
                }
            }
            ex.printStackTrace()
        }
    }
}