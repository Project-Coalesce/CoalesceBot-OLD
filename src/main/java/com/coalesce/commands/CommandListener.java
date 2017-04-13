package com.coalesce.commands;

import com.coalesce.Bot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;

public class CommandListener extends ListenerAdapter {
    
    private final CommandMap commandMap;
    
    {
        commandMap = new CommandMap();
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String commandLine = event.getMessage().getRawContent();
        
        if (!commandLine.startsWith(Bot.COMMAND_PREFIX)) {
            return;
        }
        
        commandLine = commandLine.substring(Bot.COMMAND_PREFIX.length());
        
        String[] parts = commandLine.split(" ");
        
        // not sure how this can happen to be fair - Alice
        if (parts.length < 1) {
            return;
        }
        
        String cmd = parts[0].toLowerCase();
     
        CommandMap.CommandEntry entry = commandMap.getEntry(cmd);
        
        if (entry == null) {
            // TODO: Add error message. Exception maybe?
            return;
        }
        
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
    
        try {
            CommandExecutor executor = entry.clazz.newInstance();
            executor.jda = event.getJDA();
            
            executor.execute(event.getChannel(), event.getMessage(), args);
        } catch (Exception ex) {
            // TODO: Add error message. Exception maybe?
            System.err.println("An error occurred while executing command " + cmd);
            ex.printStackTrace();
        }
    }
}
