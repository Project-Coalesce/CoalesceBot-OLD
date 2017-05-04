package com.coalesce.bot.commands.executors

import com.coalesce.bot.Colour
import com.coalesce.bot.commands.CommandType
import com.coalesce.bot.commands.Embeddables
import com.coalesce.bot.commands.RootCommand
import com.coalesce.bot.commands.RootCommandContext
import com.coalesce.bot.temperatureCelsius
import com.coalesce.bot.temperatureFahrenheit
import com.coalesce.bot.temperatureKelvin
import com.coalesce.bot.utilities.TemperatureUnit
import com.coalesce.bot.utilities.parseDouble
import java.util.concurrent.TimeUnit

class Temperature : Embeddables {
    @RootCommand(
            name = "temperature",
            type = CommandType.INFORMATION,
            permission = "commands.temperature",
            aliases = arrayOf("temp"),
            description = "Converts to different temperatures.",
            userCooldown = 10.0
    )
    fun execute(context: RootCommandContext) {
        if (context.args.size != 2) {
            context.send(context.author, "You'll need to specify the temperature and unit. (<temp> <unit: C/F/K>)")
            return
        }
        val unit = getUnit(context.args[1])
        if (unit == null) {
            context.send(context.author, "The specified unit doesn't exist. Try one of the following: Celsius, Kelvin, Fahrenheit")
            return
        }
        val temp = context.args[0].parseDouble()
        if (temp == null) {
            context.send(context.author, "The specified temperature isn't valid.")
            return
        }
        context.send(
                embed()
                        .data("Temperature Conversion", colour = Colour.GREEN, author = context.author.name, avatar = context.author.avatarUrl)
                        .field("Celsius", TemperatureUnit.CELSIUS.convertStr(temp, unit), true)
                        .field("Kelvin", TemperatureUnit.KELVIN.convertStr(temp, unit), true)
                        .field("Fahrenheit", TemperatureUnit.FAHRENHEIT.convertStr(temp, unit), true)
                        .build()
        ) { delete().queueAfter(35, TimeUnit.SECONDS) }
    }

    private fun getUnit(string: String): TemperatureUnit? {
        var matcher = temperatureCelsius.matcher(string)
        if (matcher.matches()) {
            return TemperatureUnit.CELSIUS
        }
        matcher = temperatureKelvin.matcher(string)
        if (matcher.matches()) {
            return TemperatureUnit.KELVIN
        }
        matcher = temperatureFahrenheit.matcher(string)
        if (matcher.matches()) {
            return TemperatureUnit.FAHRENHEIT
        }
        return null
    }
}