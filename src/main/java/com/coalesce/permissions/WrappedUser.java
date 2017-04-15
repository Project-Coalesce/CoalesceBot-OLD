package com.coalesce.permissions;

import com.coalesce.Bot;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Member;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WrappedUser {
    private final @Getter Member member;
    private final @Getter Map<String, Boolean> permissions = new HashMap<>();

    public WrappedUser(final Member member) {
        this.member = member;
        File guildDirectory = new File(Bot.DATA_DIRECTORY, member.getGuild().getId());
        if (!guildDirectory.exists()) {
            return;
        }
        File userFile = new File(guildDirectory, member.getUser().getId());
        if (!userFile.exists()) {
            return;
        }
        try (Reader stream = new FileReader(userFile);
             BufferedReader reader = new BufferedReader(stream)) {
            // TODO: Load permissions from json's Map<String permission, Boolean whether or not its enabled>
        } catch (IOException ex) {
            System.out.println("Couldn't load the data for user " + member.getUser().getIdLong() + ": \n" + ex.getMessage());
        }
    }
}
