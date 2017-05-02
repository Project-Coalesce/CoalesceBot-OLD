package com.coalesce.commands.executors

import com.coalesce.Constants
import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import com.coalesce.utils.TemperatureUnit
import com.coalesce.utils.parseDouble
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.util.concurrent.TimeUnit

@Command(name = "temperature", aliases = arrayOf("temp"), description = "Converts to different temperatures.", usage = "<temp> <unit>", permission = "commands.temperature",
        cooldown = 5, type = CommandType.INFORMATION)
class Temperature : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        if (args.size != 2) {
            throw CommandError("Please use the correct syntax: %s", annotation.usage)
        }
        val unit = getUnit(args[1]) ?: throw CommandError("Please enter a valid temperature of following: K(elvin), F(ahrenheit), C(elsius)")
        val temp = args[0].parseDouble() ?: throw CommandError("Please enter a valid temperature.")
        message.channel.sendMessage(EmbedBuilder()
                .setAuthor(message.author.name, null, message.author.avatarUrl)
                .setColor(Color.GREEN)
                .addField("Celsius", java.lang.Double.toString(TemperatureUnit.CELSIUS.convert(temp, unit)) + 'C', true)
                .addField("Kelvin", java.lang.Double.toString(TemperatureUnit.KELVIN.convert(temp, unit)) + 'K', true)
                .addField("Fahrenheit", java.lang.Double.toString(TemperatureUnit.FAHRENHEIT.convert(temp, unit)) + 'F', true)
                .build()).queue { it.delete().queueAfter(15, TimeUnit.SECONDS) }
    }

    private fun getUnit(string: String): TemperatureUnit? {
        var matcher = Constants.TEMPERATURE_CELSIUS.matcher(string)
        if (matcher.matches()) {
            return TemperatureUnit.CELSIUS
        }
        matcher = Constants.TEMPERATURE_KELVIN.matcher(string)
        if (matcher.matches()) {
            return TemperatureUnit.KELVIN
        }
        matcher = Constants.TEMPERATURE_FAHRENHEIT.matcher(string)
        if (matcher.matches()) {
            return TemperatureUnit.FAHRENHEIT
        }
        return null
    }
}