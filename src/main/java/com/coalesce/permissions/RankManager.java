package com.coalesce.permissions;

import com.coalesce.Bot;
import com.coalesce.utils.Streams;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.*;

public class RankManager {
    private static RankManager instance;
    private final static Object lock = new Object();

    public static RankManager getInstance() {
        RankManager inst = instance;
        if (inst == null) {
            synchronized (lock) {
                inst = instance;
                if (inst == null) {
                    inst = new RankManager();
                    instance = inst;
                }
            }
        }
        return inst;
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // TODO: Update the map with ranks from json files.
    private final @Getter Map<Role, Map<String, Boolean>> ranks = new HashMap<>();
    private final @Getter Set<WrappedUser> users = new HashSet<>();

    private RankManager() {
        Bot.getInstance().getJda().getGuilds().stream().map(Guild::getMembers).parallel().forEach(members -> members.stream().forEach(member -> users.add(new WrappedUser(member))));
    }

    public Map<String, Boolean> getPermissions(Member member) {
        Optional<WrappedUser> optional = users.stream().filter(it -> it.getMember().equals(member)).findFirst();
        if (!optional.isPresent()) {
            return new HashMap<>();
        }
        Map<String, Boolean> applicablePerms = new HashMap<>();
        WrappedUser user = optional.get();
        Streams.reverse(ranks.entrySet().stream()
                .filter(it -> member.getRoles().contains(it.getKey()))
                .sorted()).forEachOrdered(it -> applicablePerms.putAll(it.getValue()));
        applicablePerms.putAll(user.getPermissions());
        return applicablePerms;
    }
}