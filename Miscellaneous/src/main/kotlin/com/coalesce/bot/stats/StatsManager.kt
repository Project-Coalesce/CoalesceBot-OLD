package com.coalesce.bot.stats

import com.coalesce.bot.command.JDAEventHandler
import com.coalesce.bot.command.JDAListener
import com.coalesce.bot.utilities.Timeout
import com.coalesce.bot.utilities.timeOutHandler
import com.google.common.base.Optional
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent
import org.joda.time.*
import java.util.concurrent.TimeUnit

internal val epoch = MutableDateTime().apply { setDate(0L) }
private val hoursSinceEpoch: Long
    get() = Hours.hoursBetween(epoch, DateTime()).hours.toLong()
private val daysSinceEpoch: Int
    get() = Days.daysBetween(epoch, DateTime()).days
private val monthsSinceEpoch: Int
    get() = Months.monthsBetween(epoch, DateTime()).months

class StatsManager {
    private val changes = mutableMapOf<User, MutableMap<DataType, SingleDataTypeUserStorageChange>>()
    class SingleDataTypeUserStorageChange(var sum: Long, val dayMap: MutableMap<Int, Pair<Int /* Sum */, MutableList<Short>>>,
          val monthMap: MutableMap<Int, Int>) {
        private var currentSumAmount = 0
        private var timeout = Optional.absent<Timeout>()

        fun sum(amount: Int) {
            currentSumAmount += amount
            if (!timeout.isPresent) timeout = Optional.of(timeOutHandler(TimeUnit.HOURS.toMillis(hoursSinceEpoch + 1) -
                    System.currentTimeMillis(), TimeUnit.MILLISECONDS) {
                addHourLongSum(currentSumAmount.toShort())
                currentSumAmount = 0
                timeout = Optional.absent()
            })
        }

        private fun addHourLongSum(value: Short) {
            sum += value
            val dse = daysSinceEpoch
            dayMap[dse] = (dayMap[dse] ?: 0 to mutableListOf()).run { (first + value) to second.apply { add(value) } }
            val mse = monthsSinceEpoch
            monthMap[mse] = (monthMap[mse] ?: 0) + value
        }
    }

    fun invoke(user: User, dataType: DataType, amount: Int) {
        changes[user] = (changes[user] ?: mutableMapOf()).apply {
            val change = this[dataType] ?: run { /* TODO Load */  return }
            change.sum(amount)
        }
    }
}

enum class DataType {
    ONLINE_TIME, OFFLINE_TIME, AFK_TIME, DND_TIME, MESSAGES_SENT, TYPING_TIME, IN_VOICE_CHANNEL_TIME
}

@JDAEventHandler
class Listener {
    @JDAListener
    fun presenceChange(event: UserOnlineStatusUpdateEvent) {
        
    }
}