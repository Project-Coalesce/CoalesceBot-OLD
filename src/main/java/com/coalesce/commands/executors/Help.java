package com.coalesce.commands.executors;

import com.coalesce.commands.Command;
import com.coalesce.commands.CommandExecutor;
import com.coalesce.commands.CommandMap;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(name = "help", description = "Show bot help", aliases = { "?", "h" })
public class Help extends CommandExecutor {
    
    @Override
    protected void execute(MessageChannel channel, Message message, String[] args) throws Exception {
        MessageBuilder mb = new MessageBuilder();
        mb.append("```\n");
        
        List<CommandMap.CommandEntry> entries = new ArrayList<>();
        
        commandMap.getEntries().values().forEach(e -> {
            if (!entries.contains(e)) {
                entries.add(e);
                mb.append(e.getMeta().name()).append(" ").append(e.getMeta().usage()).append(" ")
                        .append(e.getMeta().aliases().length == 0
                                ? ""
                                : Arrays.toString(e.getMeta().aliases()))
                        .append("\n");
            }
        });
        
        entries.clear();
        
        mb.append("```");
        
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Help", null)
                .setColor(Color.GREEN)
                .setDescription(mb.build().getRawContent())
                .build()).queue();
    }
}