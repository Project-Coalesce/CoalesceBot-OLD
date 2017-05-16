package com.coalesce.bot.commands

import com.coalesce.bot.Colour
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.lang.reflect.Method

enum class CommandType {
    FUN,
    INFORMATION,
    ADMINISTRATION,
    DEBUG,
    MUSIC,
    MISCELLANEOUS,
    HIDDEN
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RootCommand(// All root commands are annotated with this. 1 root command per class
        val name: String,
        val permission: String,
        val type: CommandType = CommandType.HIDDEN,
        val usage: String = "",
        val description: String = "",
        val aliases: Array<String> = arrayOf(),
        val globalCooldown: Double = 0.0,
        val userCooldown: Double = 0.0,
        val recursiveGlobalCooldown: Double = 0.0, // If the sub doesnt specify theirs (have it 0.0), it'll check the recursives.
        val recursiveUserCooldown: Double = 0.0,
        val cooldownSubs: Boolean = false // Apply the same cooldown to all subs when executing the root.
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SubCommand(
        val name: String,
        val permission: String = "", // Automatically inherit parent
        val usage: String = "",
        val aliases: Array<String> = arrayOf(),
        val globalCooldown: Double = 0.0,
        val userCooldown: Double = 0.0,
        val cooldown: Boolean = true // Whether or not to even have the cooldown feature
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class JDAListener

abstract class CommandContext(
        open val jda: JDA,
        open val selfUser: SelfUser,
        open val message: Message,
        open val event: MessageReceivedEvent,
        open val author: User,
        open val channel: MessageChannel,
        open val rootCommand: RootCommand,
        open val subcommands: Map<String, Pair<Method, SubCommand>>, // There will be a resolve method.
        open val args: Array<String>
) {
    inline operator fun invoke(text: String, crossinline after: Message.() -> Unit) {
        send(text) { after(this) }
    }

    inline operator fun invoke(text: Message, crossinline after: Message.() -> Unit) {
        send(text) { after(this) }
    }

    inline operator fun invoke(text: MessageEmbed, crossinline after: Message.() -> Unit) {
        send(text) { after(this) }
    }

    inline operator fun invoke(mention: IMentionable, text: String, crossinline after: Message.() -> Unit) {
        send(mention, text) { after(this) }
    }

    inline operator fun invoke(text: MessageBuilder, crossinline after: Message.() -> Unit) {
        send(text) { after(this) }
    }

    inline operator fun invoke(text: EmbedBuilder, crossinline after: Message.() -> Unit) {
        send(text) { after(this) }
    }

    operator fun invoke(text: String) {
        send(text)
    }

    operator fun invoke(text: Message) {
        send(text)
    }

    operator fun invoke(text: MessageEmbed) {
        send(text)
    }

    operator fun invoke(mention: IMentionable, text: String) {
        send(mention, text)
    }

    operator fun invoke(text: MessageBuilder) {
        send(text)
    }

    operator fun invoke(text: EmbedBuilder) {
        send(text)
    }

    fun send(message: Message) {
        channel.sendMessage(message).queue()
    }

    inline fun send(message: Message, crossinline after: Message.() -> Unit) {
        channel.sendMessage(message).queue { after(it) }
    }

    fun send(embed: MessageEmbed) {
        channel.sendMessage(embed).queue()
    }

    inline fun send(embed: MessageEmbed, crossinline after: Message.() -> Unit) {
        channel.sendMessage(embed).queue { after(it) }
    }

    fun send(text: String) {
        channel.sendMessage(text).queue()
    }

    inline fun send(text: String, crossinline after: Message.() -> Unit) {
        channel.sendMessage(text).queue { after(it) }
    }

    fun send(mention: IMentionable, text: String) {
        send("${mention.asMention}: $text")
    }

    inline fun send(mention: IMentionable, text: String, crossinline after: Message.() -> Unit) {
        send("${mention.asMention}: $text") { after(this) }
    }

    fun send(builder: MessageBuilder) {
        send(builder.build())
    }

    inline fun send(builder: MessageBuilder, crossinline after: Message.() -> Unit) {
        send(builder.build()) { after(this) }
    }

    fun send(builder: EmbedBuilder) {
        send(builder.build())
    }

    inline fun send(builder: EmbedBuilder, crossinline after: Message.() -> Unit) {
        send(builder.build()) { after(this) }
    }
}

class RootCommandContext(
        override val jda: JDA,
        override val selfUser: SelfUser,
        override val message: Message,
        override val event: MessageReceivedEvent,
        override val author: User,
        override val channel: MessageChannel,
        override val rootCommand: RootCommand,
        override val subcommands: Map<String, Pair<Method, SubCommand>>,
        override val args: Array<String>
) : CommandContext(jda, selfUser, message, event, author, channel, rootCommand, subcommands, args)

class SubCommandContext(
        override val jda: JDA,
        override val selfUser: SelfUser,
        override val message: Message,
        override val event: MessageReceivedEvent,
        override val author: User,
        override val channel: MessageChannel,
        override val rootCommand: RootCommand,
        override val subcommands: Map<String, Pair<Method, SubCommand>>,
        override val args: Array<String>,
        val currentSubCommand: SubCommand
) : CommandContext(jda, selfUser, message, event, author, channel, rootCommand, subcommands, args)

interface Embeddables {
    fun embed(): EmbedBuilder {
        return EmbedBuilder()
    }

    fun makeField(title: String?, text: String, inline: Boolean = false): MessageEmbed.Field {
        return MessageEmbed.Field(title, text, inline)
    }

    fun makeField(title: String?, user: IMentionable, inline: Boolean = false): MessageEmbed.Field {
        return makeField(title, user.asMention, inline)
    }

    fun EmbedBuilder.field(title: String?, text: String, inline: Boolean = false): EmbedBuilder {
        return this.addField(makeField(title, text, inline))
    }

    fun EmbedBuilder.field(title: String?, user: IMentionable, inline: Boolean = false): EmbedBuilder {
        return field(title, user.asMention, inline)
    }

    fun EmbedBuilder.data(title: String?, colour: Colour? = null, author: String? = null, avatar: String? = null, url: String? = null): EmbedBuilder {
        return apply {
            setTitle(title, url)
            setAuthor(author, null, avatar)
            setColour(colour)
        }
    }
}

/**
 * Sets the Color of the embed.
 * @param colour the color of the embed
 * @return the builder after the color has been set
 */
fun EmbedBuilder.setColour(colour: Colour?): EmbedBuilder = this.setColor(colour)