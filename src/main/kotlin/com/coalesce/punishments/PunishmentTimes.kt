package com.coalesce.punishments

import java.util.*

enum class PunishmentTimes(var timeUnit: Int, var time: Int) {

    WARNING(-1, -1), SECOND_WARNING(-1, -1), THIRD(Calendar.HOUR, 1), FOURTH(Calendar.HOUR, 8), FIFTH(Calendar.DAY_OF_MONTH, 2),
    SIXTH(Calendar.WEEK_OF_MONTH, 1), SEVENTH(Calendar.YEAR, 666)

}