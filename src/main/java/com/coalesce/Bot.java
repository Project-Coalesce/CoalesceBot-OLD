package com.coalesce;

import com.coalesce.commands.CommandListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Bot {
    public static final String COMMAND_PREFIX = "!";
    public static final File DATA_DIRECTORY = new File("data");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static Bot instance;
    private @Getter JDA jda;
    private @Getter CommandListener commandListener;
    public ExecutorService executor = Executors.newFixedThreadPool(6);

    public static Bot getInstance() {
        return instance;
    }

    void run(String token) throws Exception {
        instance = this;
        if (!DATA_DIRECTORY.exists()) {
            if (DATA_DIRECTORY.mkdirs()) {
                System.out.println("The data directory didn't exist already and was created.");
            }
        }

        jda = new JDABuilder(AccountType.BOT).setAudioEnabled(false).setCorePoolSize(4).setToken(token).buildBlocking();
        jda.getGuilds().stream()
                .map(Guild::getPublicChannel)
                .filter(TextChannel::canTalk)
                .forEach(it -> it.sendMessage("The bot is now enabled and ready for user input.").queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS)));

        jda.addEventListener(commandListener = new CommandListener(this));
    }
}
