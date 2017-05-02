package com.coalesce.punishmentals

import java.util.*

enum class Reason(val description: String, val severity: Int) {
    //Severity 1
    GENERAL_WARNING("General warning with custom description", 1),
    SPAM("Spamming the chat", 1),
    LONG_LOG_POSTS("Long log posting, please use Pastebin", 1),
    WRONG_SECTION("Off topic conversation", 1),

    //Severity 2
    RUDENESS("General rudeness or lack of respect", 2),
    PORN("Porn", 2),
    GORE("Gore", 2)
}

enum class PunishmentTimes(val timeUnit: Int, val time: Int, val permanent: Boolean = false) {
    WARNING(0, 0),
    SECOND_WARNING(0, 0),
    THIRD(Calendar.HOUR, 1),
    FOURTH(Calendar.HOUR, 8),
    FIFTH(Calendar.DAY_OF_MONTH, 2),
    SIXTH(Calendar.WEEK_OF_MONTH, 1),
    SEVENTH(-1, -1, permanent = true)
}