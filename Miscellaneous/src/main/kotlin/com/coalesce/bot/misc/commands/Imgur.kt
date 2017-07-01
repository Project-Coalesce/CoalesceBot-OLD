package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.gson.JsonElement
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import sun.management.resources.agent
import java.awt.Color
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService

@Command("Imgur", "uploadimage image img uploadimg")
class Imgur @Inject constructor(val executorService: ExecutorService): Embeddables {
    private val client = HttpClients.createDefault()

    @CommandAlias("Uploads image to imgur, gets the first message with image in the history if none are found.")
    fun previousMessages(context: CommandContext) {
        if (context.message.attachments.isNotEmpty() && context.message.attachments.any { it.isImage }) {
            context(embed().apply {
                embColor = Color.YELLOW
                embTitle = "Uploading..."
            }) {
                executorService.submit {
                    upload(context, this, context.message.attachments.first { it.isImage }.url)
                }
            }
            return
        }

        context.channel.history.retrievePast(10).queue {
            val message = it.firstOrNull { it.attachments.isNotEmpty() && it.attachments.any { it.isImage } } ?: throw ArgsException("No messages found!\nYou can use !imgur and attach an image instead.")
            context(embed().apply {
                embColor = Color.YELLOW
                embTitle = "Uploading..."
            }) {
                executorService.submit {
                    upload(context, this, message.attachments.first { it.isImage }.url)
                }
            }
        }
    }

    private fun upload(context: Context, message: Message, image: String) {
        try {
            val post = HttpPost("https://api.imgur.com/3/image")
            post.entity = UrlEncodedFormEntity(listOf(
                    "image" to image
            ).map { BasicNameValuePair(it.first, it.second) })
            post.addHeader("Authorization", "Client-ID 5133141b12a8791")
            val json = gson.fromJson(client.execute(post).entity.content.readText(), JsonElement::class.java).asJsonObject

            message.editMessage(EmbedBuilder(message.embeds.first()).apply {
                val lnk = json["data"].asJsonObject["link"].asString
                embColor = Color(112, 255, 45)
                setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                setTitle("Imgur Image Uploaded", lnk)
                setImage(lnk)
            }.build()).queue()
        } catch (ex: Exception) {
            message.editMessage(EmbedBuilder(message.embeds.first()).apply {
                embTitle = "Error"
                embColor = Color(232, 46, 0)

                description {
                    appendln("Failed to send image!")
                    appendln("${ex.javaClass.name}: ${ex.message}")
                    appendln("This has been reported to coalesce developers.")
                }
            }.build()).queue()
            ex.printStackTrace()
        }
    }
}