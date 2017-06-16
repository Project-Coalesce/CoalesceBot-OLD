package com.coalesce.bot.commands.executors

import com.coalesce.bot.Main
import com.coalesce.bot.binary.LongSerializer
import com.coalesce.bot.commands.*
import com.coalesce.bot.rulesMessageFile
import com.coalesce.bot.utilities.subList
import com.google.inject.Inject
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color

class RulesMessage @Inject constructor(val bot: Main): Embeddables {
    @RootCommand(
            name = "RulesMessage",
            permission = "commands.rulesMessage",
            type = CommandType.ADMINISTRATION,
            description = "Edit the rules message.",
            aliases = arrayOf("rules-message", "editrules")
    )
    fun execute(context: RootCommandContext) {
        val args = context.args.joinToString(separator = " ").split("\n")
        if (args.size < 2) {
            throw ArgsException("Usage: ``!editrules <field> (New Line) <position> (New line) <text>``")
        }
        val field = args[0]
        val position = args[1].toIntOrNull() ?: throw ArgsException("Position especified is not a number.")
        val text = args.subList(2).joinToString(separator = "\n")

        fun processMessage(message: Message) {
            message.embeds[0].apply {
                val otherFields = fields.filter { it.name != field }.toMutableList()
                val addedField = MessageEmbed.Field(field, text, false)
                otherFields.add(position, addedField)

                message.editMessage(EmbedBuilder(message.embeds[0]).apply {
                    clearFields()
                    otherFields.forEach { addField(it) }
                }.build()).queue()
            }
        }

        if (rulesMessageFile.exists()) context.channel.getMessageById(LongSerializer(rulesMessageFile).read()).queue(::processMessage)
        else {
            context(embed().apply {
                setColor(Color(0xBE58B6))
                setTitle("Welcome to ${context.message.guild.name}!", null)
            }) {
                LongSerializer(rulesMessageFile).write(idLong)
                processMessage(this)
            }
        }
    }
}