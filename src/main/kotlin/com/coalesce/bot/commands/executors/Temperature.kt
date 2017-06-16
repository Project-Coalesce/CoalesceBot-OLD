package com.coalesce.bot.commands.executors

import com.coalesce.bot.*
import com.coalesce.bot.commands.*
import com.coalesce.bot.utilities.TemperatureUnit
import com.coalesce.bot.utilities.ifwithDo
import com.coalesce.bot.utilities.parseDouble
import java.nio.file.Files.delete
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
            throw ArgsException("You need to specify the temperature and unit. (<temp> <unit: C/F/K>)")
        }
        val unit = getUnit(context.args[1]) ?:
                throw ArgsException("The specified unit doesn't exist. Try one of the following: Celsius, Kelvin, Fahrenheit")
        val temp = context.args[0].parseDouble() ?: throw ArgsException("The specified temperature isn't valid.")

        context(embed()
                .data("Temperature Conversion", colour = Colour.GREEN, author = context.author.name, avatar = context.author.avatarUrl)
                .field("Celsius", TemperatureUnit.CELSIUS.convertStr(temp, unit), true)
                .field("Kelvin", TemperatureUnit.KELVIN.convertStr(temp, unit), true)
                .field("Fahrenheit", TemperatureUnit.FAHRENHEIT.convertStr(temp, unit), true)
        ) { ifwithDo(canDelete, context.message.guild) { delete().queueAfter(35, TimeUnit.SECONDS) } }
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