package com.coalesce.bot.command

import com.coalesce.bot.utilities.matching
import com.coalesce.bot.utilities.smallTimeUnit
import com.coalesce.bot.utilities.subList
import com.coalesce.bot.utilities.tryOrNull
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.User
import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.kotlinFunction

class AdaptationArgsChecker(val jda: JDA) {
    private val adaptationsMap = mutableMapOf<Class<*>, (String) -> Any?>(
            String::class.java to this::stringAdapter,
            User::class.java to this::userAdapter,
            Role::class.java to this::roleAdapter,
            Calendar::class.java to this::timeAdapter,
            Int::class.java to String::toIntOrNull,
            Long::class.java to String::toIntOrNull,
            Double::class.java to String::toDoubleOrNull
    )

    fun adapt(args: Array<String>, type: Class<*>): Pair<Array<String>, Any>? {
        val newArgs = (if (args.size <= 1) arrayOf<String>() else args.toList().subList(1).toTypedArray())
        if (adaptationsMap.containsKey(type)) {
            return newArgs to (adaptationsMap[type]!!(args[0]) ?: return null)
        } else if (type.isEnum) {
            val constants = type.enumConstants
            return newArgs to (constants.find { it.toString().toLowerCase() == args[0].toLowerCase() } ?: return null)
        } else {
            return adaptWithReflection(args, type)
        }
    }

    fun adaptWithReflection(args: Array<String>, clazz: Class<*>): Pair<Array<String>, Any>? {
        clazz.declaredConstructors.forEach {
            if (it.parameterCount <= args.size) {
                var arguments = args.copyOf()
                if (it.kotlinFunction != null) {
                    val kFunc = it.kotlinFunction!!
                    val parameters = mutableMapOf<KParameter, Any>()

                    kFunc.parameters.forEachIndexed { index, param ->
                        val (newArgs, obj) = adapt(arguments, param.type as Class<*>) ?:
                                if (param.isOptional) return@forEachIndexed else return@forEach
                        arguments = newArgs; parameters[param] = obj
                    }

                    if (!it.isAccessible) tryOrNull { it.isAccessible = true } ?: return@forEach
                    val item = kFunc.callBy(parameters)
                    return arguments to item
                }

                val items = mutableListOf<Any?>()

                it.parameterTypes.forEachIndexed { index, param ->
                    val (newArgs, obj) = adapt(arguments, param) ?: return@forEach
                    arguments = newArgs; items.add(obj)
                }

                if (!it.isAccessible) tryOrNull { it.isAccessible = true } ?: return@forEach
                val item = it.newInstance(items)
                return arguments to item
            }
        }

        return null
    }

    fun stringAdapter(str: String) = str

    fun timeAdapter(str: String): Calendar? {
        val cal = Calendar.getInstance()
        if (str.matches(Regex("[0-9]+[dhms]"))) {
            val time = str.substring(0, str.length - 1).toLongOrNull() ?: return null
            val unit = str.substring(str.length - 1).smallTimeUnit() ?: return null
            cal.timeInMillis += unit.toMillis(time)
        }

        return cal
    }

    fun userAdapter(str: String): User? {
        if (!str.matches(Regex("<@[0-9]+>"))) return null
        return jda.getUserById(str.matching(Regex("[0-9]")))
    }

    fun roleAdapter(str: String): Role? {
        if (!str.matches(Regex("<@&[0-9]+>"))) return null
        return jda.getRoleById(str.matching(Regex("[0-9]")))
    }
}
