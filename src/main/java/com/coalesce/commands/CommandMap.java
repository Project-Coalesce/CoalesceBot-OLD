package com.coalesce.commands;

import com.coalesce.Bot;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandMap {
    
    private final Bot bot;
    private final Map<String, CommandEntry> entries = new LinkedHashMap<>();
    
    CommandMap(Bot bot) {
        this.bot = bot;
        
        detectCommands();
    }
    
    private void register(Class<? extends CommandExecutor> clazz) {
        try {
            System.out.println("Registering command: " + clazz.getSimpleName());
            CommandEntry entry = new CommandEntry(clazz, this);
            
            registerCommand(entry.meta.name().toLowerCase(), entry);
            
            Arrays.stream(entry.meta.aliases())
                    .map(String::toLowerCase)
                    .forEach(a -> registerCommand(a, entry));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void registerCommand(String label, CommandEntry entry) {
        entries.put(label, entry);
    }
    
    public CommandEntry getEntry(String string) {
        return entries.get(string.toLowerCase());
    }
    
    private void detectCommands() {
        Reflections ref = new Reflections("com.coalesce.commands.executors");
        
        ref.getSubTypesOf(CommandExecutor.class).forEach(this::register);
    }
    
    public Map<String, CommandEntry> getEntries() {
        return entries;
    }
    
    public class CommandEntry {
        final CommandExecutor executor;
        final Command meta;
        
        private CommandEntry(Class<? extends CommandExecutor> clazz, CommandMap map) throws Exception {
            this.executor = clazz.newInstance();
            this.executor.jda = bot.getJDA();
            this.executor.commandMap = map;
            this.meta = clazz.getAnnotation(Command.class);
            this.executor.annotation = meta;
        }
        
        public Command getMeta() {
            return meta;
        }
    }
}