package com.coalesce.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
    String name();

    String usage() default "";

    String description() default "";

    String[] aliases() default {};

    double permission() default 0.0;
}
