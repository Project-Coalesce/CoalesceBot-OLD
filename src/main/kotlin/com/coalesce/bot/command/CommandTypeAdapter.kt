package com.coalesce.bot.command

import com.coalesce.bot.utilities.matching
import com.coalesce.bot.utilities.subList
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.User

class AdaptationArgsChecker(val jda: JDA) {
    private val adaptationsMap = mutableMapOf<Class<*>, (String) -> Any?>(
            String::class.java to this::stringAdapter,
            User::class.java to this::userAdapter,
            Int::class.java to String::toIntOrNull,
            Long::class.java to String::toIntOrNull,
            Double::class.java to String::toDoubleOrNull
    )

    fun adapt(args: Array<String>, type: Class<*>): Pair<Array<String>, Any>? {
        if (adaptationsMap.containsKey(type)) {
            return (if (args.size <= 1) arrayOf<String>() else args.toList().subList(1).toTypedArray()) to
                    (adaptationsMap[type]!!(args[0]) ?: return null)
        } else {
            return adaptWithReflection(args, type)
        }
    }

    fun attemptAdaptation(classes: Array<Class<*>>, args: Array<String>): Array<Any>? {
        if (args.size != classes.size) return null
        val objects = mutableListOf<Any>()
        classes.forEachIndexed { index, it -> objects.add(adapt(arrayOf(args[index]), it) ?: return null) }
        return objects.toTypedArray()
    }

    fun adaptWithReflection(args: Array<String>, clazz: Class<*>): Pair<Array<String>, Any>? {
        clazz.declaredConstructors.forEach {
            if (it.parameterCount <= args.size) {
                val items = mutableListOf<Any>()
                it.parameterTypes.forEachIndexed { index, param ->
                    val respectiveArg = args[index]
                    items.add(adapt(arrayOf(respectiveArg), param) ?: return@forEach)
                }

                if (!it.isAccessible) try {
                    it.isAccessible = true
                } catch (ex: Exception) {
                    return@forEach
                }

                val outArgs = args.toList().subList(0, it.parameterCount).toTypedArray()
                val item = it.newInstance(items)

                return outArgs to item
            }
        }

        return null
    }

    fun stringAdapter(str: String) = str

    fun userAdapter(str: String): User? {
        if (!str.matches(Regex("<@[0-9]+>"))) return null
        return jda.getUserById(str.matching(Regex("[0-9]"))) //TODO find a way to get around instance
    }
}
