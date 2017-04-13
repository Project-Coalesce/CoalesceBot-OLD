package com.coalesce.commands.executors;

import com.coalesce.commands.Command;
import com.coalesce.commands.CommandError;
import com.coalesce.commands.CommandExecutor;
import com.coalesce.utils.Parsing;
import com.coalesce.utils.Patterns;
import com.coalesce.utils.TemperatureUnit;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.Optional;
import java.util.regex.Matcher;

@Command(name = "temperature", aliases = {"temp"}, description = "Converts to different temperatures.", usage = "<temp> <unit>")
public class Temperature extends CommandExecutor {
    @Override
    protected void execute(MessageChannel channel, Message message, String[] args) throws Exception {
        if (args.length != 2) {
            throw new CommandError("Please use the correct syntax: %s", getAnnotation().usage());
        }
        TemperatureUnit unit = getUnit(args[1]).orElseThrow(() -> new CommandError("Please enter a valid temperature of following: K(elvin), F(ahrenheit), C(elsius)"));
        double temp = Parsing.parseDouble(args[0]).orElseThrow(() -> new CommandError("Please enter a valid temperature."));
        if (message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            message.delete().queue();
        }
        message.getChannel().sendMessage(new EmbedBuilder()
                .addField("Celsius", Double.toString(unit.convert(temp, TemperatureUnit.CELSIUS)) + 'C', true)
                .addField("Kelvin", Double.toString(unit.convert(temp, TemperatureUnit.KELVIN)) + 'K', true)
                .addField("Fahrenheit", Double.toString(unit.convert(temp, TemperatureUnit.FAHRENHEIT)) + 'F', true)
                .build()).queue();
    }

    private Optional<TemperatureUnit> getUnit(String string) {
        Matcher matcher = Patterns.TEMPERATURE_CELSIUS.matcher(string);
        if (matcher.matches()) {
            return Optional.of(TemperatureUnit.CELSIUS);
        }
        matcher = Patterns.TEMPERATURE_KELVIN.matcher(string);
        if (matcher.matches()) {
            return Optional.of(TemperatureUnit.KELVIN);
        }
        matcher = Patterns.TEMPERATURE_FAHRENHEIT.matcher(string);
        if (matcher.matches()) {
            return Optional.of(TemperatureUnit.FAHRENHEIT);
        }
        return Optional.empty();
    }
}
