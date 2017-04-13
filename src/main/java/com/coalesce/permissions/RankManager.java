package com.coalesce.permissions;

import net.dv8tion.jda.core.entities.Role;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<Role, Double> ranks = new HashMap<>();

    private RankManager() {
    }
}