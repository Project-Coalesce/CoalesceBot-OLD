package com.coalesce.permissions;

import net.dv8tion.jda.core.entities.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private final Map<Role, Double> ranks = new HashMap<>();

    private RankManager() {
    }

    @SuppressWarnings("WeakerAccess") // Shall not be weaker due to API.
    public Optional<Double> getRank(Role role) {
        if (role == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ranks.get(role));
    }

    public boolean isAllowed(Role role, double rank) {
        return getRank(role).filter(it -> it >= rank).isPresent();
    }
}