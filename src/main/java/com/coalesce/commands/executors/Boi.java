package com.coalesce.commands.executors;

import com.coalesce.commands.Command;
import com.coalesce.commands.CommandError;
import com.coalesce.commands.CommandExecutor;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

@Command(name = "boi", aliases = {"njsblessing"}, description = "Posts a boi meme into chat.", permission = "commands.boi")
public class Boi extends CommandExecutor {
    @Override protected void execute(MessageChannel channel, Message message, String[] args) throws Exception {
        String url = message.getAuthor().getName().matches("[0-9a-zA-Z]+") ?
                "http://i1.kym-cdn.com/photos/images/newsfeed/001/183/604/ee9.png" :
                "http://i.imgur.com/hOYMcij.jpg";
        throw new CommandError(url);
    }
}