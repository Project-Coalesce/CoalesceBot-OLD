package com.coalesce.bot.misc.commands

import com.coalesce.bot.command.*
import com.coalesce.bot.gson
import com.coalesce.bot.utilities.*
import com.google.gson.JsonElement
import com.google.inject.Inject
import net.dv8tion.jda.core.entities.Message
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import java.awt.Color
import java.util.concurrent.ExecutorService

internal val client = HttpClients.createDefault()
@Command("Imgur", "uploadimage image img uploadimg")
class Imgur @Inject constructor(val executorService: ExecutorService): Embeddables {

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

    companion object {
        fun upload(image: String, type: String = "URL"): String {
            val post = HttpPost("https://api.imgur.com/3/image")
            post.entity = UrlEncodedFormEntity(listOf(
                    "image" to image,
                    "type" to type
            ).map { BasicNameValuePair(it.first, it.second) })
            post.addHeader("Authorization", "Client-ID 5133141b12a8791")

            val response = client.execute(post).entity.content.readText()
            return gson.fromJson(response, JsonElement::class.java).asJsonObject["data"]
                    .asJsonObject["link"].asString
        }
    }
    private fun upload(context: Context, message: Message, image: String) {
        try {
            val link = Companion.upload(image)
            message.editEmbed {
                embColor = Color(112, 255, 45)
                setAuthor(context.author.name, null, context.author.effectiveAvatarUrl)
                setTitle("Imgur Image Uploaded", link)
                setImage(link)
            }
        } catch (ex: Exception) {
            message.editEmbed {
                embTitle = "Error"
                embColor = Color(232, 46, 0)

                description {
                    appendln("Failed to send image!")
                    appendln("${ex.javaClass.name}: ${ex.message}")
                    appendln("This has been reported to coalesce developers.")
                }
            }
            ex.printStackTrace()
        }
    }
}