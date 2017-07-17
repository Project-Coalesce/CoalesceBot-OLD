package com.coalesce.bot

import com.coalesce.bot.binary.BinarySerializer
import com.coalesce.bot.utilities.Timeout
import com.coalesce.bot.utilities.timeOutHandler
import java.io.DataOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

open class CachedDataManager<U, A>(file: File,
                                private val serializer: BinarySerializer<MutableMap<U, A>>,
                                private val creator: () -> A): Timeout(1L, TimeUnit.MINUTES) {
    private val cache = mutableMapOf<U, A>()

    init {
        stopTimeout()
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.apply {
            createNewFile()
            outputStream().use { DataOutputStream(it).writeLong(-1L) }
        }
    }

    var rawData: MutableMap<U, A>
        get() = serializer.read()
        set(map) = serializer.write(map)

    operator fun get(from: U): A {
        return cache[from] ?: run {
            synchronized(serializer) {
                val userData = serializer.read()[from] ?: creator()
                cache[from] = userData
                timeOutHandler(1L, TimeUnit.HOURS) { cache.remove(from) }
                userData
            }
        }
    }

    fun save(user: U, value: A) {
        cache[user] = value
        if (!waiting) startTimeout()
    }

    override fun timeout() {
        rawData = rawData.apply { putAll(cache) }
        cache.clear()
    }
}