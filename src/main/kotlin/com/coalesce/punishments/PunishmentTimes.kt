package com.coalesce.punishments

import java.util.*

enum class PunishmentTimes(var timeUnit: Int, var time: Int) {

    WARNING(-1, -1), SECOND_WARNING(-1, -1), THIRD(Calendar.HOUR, 1)

}