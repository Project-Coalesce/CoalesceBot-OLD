package com.coalesce.bot.commands

import com.coalesce.bot.Main
import com.coalesce.bot.utilities.matching
import net.dv8tion.jda.core.entities.User

class AdaptationArgsChecker {
    private val adaptationsMap = mutableMapOf<Class<*>, (String) -> Any?>(
            String::class.java to ::stringAdapter,
            User::class.java to ::userAdapter,
            Int::class.java to String::toIntOrNull,
            Long::class.java to String::toIntOrNull,
            Double::class.java to String::toDoubleOrNull
    )

    fun attemptAdaptation(classes: Array<Class<*>>, args: Array<String>): Array<Any>? {
        if (args.size != classes.size) return null
        val objects = mutableListOf<Any>()
        classes.forEachIndexed { index, it -> objects.add((adaptationsMap[it] ?: return null)(args[index]) ?: return null) }
        return objects.toTypedArray()
    }
}

fun stringAdapter(str: String) = str

fun userAdapter(str: String): User? {
    if (!str.matches(Regex("<@[0-9]+>"))) return null
    return Main.instance.jda.getUserById(str.matching(Regex("[0-9]"))) //TODO find a way to get around instance
}