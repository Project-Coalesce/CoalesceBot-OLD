package com.coalesce.permissions;

import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.core.entities.Role;

import java.util.HashMap;
import java.util.Map;

public class WrappedRole {
    private final @Getter Role role;
    private final @Getter Map<String, Boolean> permissions = new HashMap<>();

    public WrappedRole(@NonNull final Role role) {
        this.role = role;
    }
}
