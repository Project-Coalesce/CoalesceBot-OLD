package com.coalesce;

import com.coalesce.commands.CommandListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class Bot {
    public static final String COMMAND_PREFIX = "!";
    private JDA jda;

    void run(String token) throws Exception {
        jda = new JDABuilder(AccountType.BOT).setAudioEnabled(false).setCorePoolSize(4).setToken(token).buildBlocking();
        for (Guild guild : jda.getGuilds()) {
            TextChannel channel = guild.getPublicChannel();
            if (!channel.canTalk()) {
                continue;
            }
            channel.sendMessage("The bot is now enabled and ready for user input.").queue();
        }
        
        jda.addEventListener(new CommandListener());
    }
    
    public JDA getJDA() {
        return jda;
    }
}
