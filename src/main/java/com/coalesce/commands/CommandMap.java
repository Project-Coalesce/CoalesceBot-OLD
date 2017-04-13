package com.coalesce.commands;

import org.reflections.Reflections;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandMap {
    private final Map<String, CommandEntry> entries = new LinkedHashMap<>();

    CommandMap() {
        detectCommands();
    }

    private void register(Class<? extends CommandExecutor> clazz) {
        System.out.println("Registering command: " + clazz.getSimpleName());
        CommandEntry entry = new CommandEntry(clazz);

        registerCommand(entry.meta.name().toLowerCase(), entry);

        Arrays.stream(entry.meta.aliases())
                .map(String::toLowerCase)
                .forEach(a -> registerCommand(a, entry));
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

    public static class CommandEntry {
        final Class<? extends CommandExecutor> clazz;
        final Command meta;

        private CommandEntry(Class<? extends CommandExecutor> clazz) {
            this.clazz = clazz;
            this.meta = clazz.getAnnotation(Command.class);
        }

        public Command getMeta() {
            return meta;
        }
    }
}