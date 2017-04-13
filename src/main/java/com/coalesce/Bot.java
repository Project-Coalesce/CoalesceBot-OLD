package com.coalesce;

import com.coalesce.commands.CommandListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class Bot {
    public static final String COMMAND_PREFIX = "!";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private JDA jda;

    void run(String token) throws Exception {
        jda = new JDABuilder(AccountType.BOT).setAudioEnabled(false).setCorePoolSize(4).setToken(token).buildBlocking();
        jda.getGuilds().stream()
                .map(Guild::getPublicChannel)
                .filter(TextChannel::canTalk)
                .forEach(it -> it.sendMessage("The bot is now enabled and ready for user input.").queue());

        jda.addEventListener(new CommandListener());
    }

    public JDA getJDA() {
        return jda;
    }
}
