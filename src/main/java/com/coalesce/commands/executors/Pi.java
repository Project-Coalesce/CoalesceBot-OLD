package com.coalesce.commands.executors;

import com.coalesce.commands.Command;
import com.coalesce.commands.CommandError;
import com.coalesce.commands.CommandExecutor;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

@Command(name = "Pi", permission = "commands.pi", description = "Shows pi.")
public class Pi extends CommandExecutor {
    @Override protected void execute(MessageChannel channel, Message message, String[] args) throws Exception {
        throw new CommandError(String.valueOf(Math.PI));
    }
}
