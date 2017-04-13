package com.coalesce.commands;

public class CommandError extends Exception {
    public CommandError(String message) {
        super(message);
    }

    public CommandError(String message, Object... args) {
        super(String.format(message, args));
    }
}
