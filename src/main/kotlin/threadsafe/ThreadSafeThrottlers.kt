/*
 *
 * Throttler: throttling implementations.
 * Copyright (C) 2024 Lucas M. de Jong Larrarte
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package io.github.lucasmdjl.throttler.threadsafe

import io.github.lucasmdjl.throttler.FixedRateThrottler
import io.github.lucasmdjl.throttler.internal.SystemTimeProvider
import io.github.lucasmdjl.throttler.internal.TimeProvider
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.read
import kotlin.concurrent.write


/**
 * A thread-safe throttler based on a specified number of allowed accesses within a given time period.
 *
 * @param count the maximum number of accesses allowed within the time period. Must be positive.
 * @param millis the time period in milliseconds. Must be positive.
 */
public class SyncFixedRateThrottler internal constructor(count: Int, millis: Long, timeProvider: TimeProvider) : ThreadSafeThrottler {
    public constructor(count: Int, millis: Long) : this(count, millis, SystemTimeProvider)
    private val unsafeFixedRateThrottler = FixedRateThrottler(count, millis, timeProvider)

    @Synchronized
    override fun access(): Boolean = unsafeFixedRateThrottler.access()
}

/**
 * A thread-safe throttler that always grants access, effectively performing no throttling.
 */
public object NoThrottler : ThreadSafeThrottler {
    override fun access(): Boolean = true
}

/**
 * A thread-safe throttler that never grants access.
 */
public object BlockingThrottler : ThreadSafeThrottler {
    override fun access(): Boolean = false
}

/**
 * A thread-safe throttler that allows dynamic resetting of its internal throttler instance.
 *
 * This class provides a mechanism to limit the rate of access to a resource or operation,
 * with the flexibility to replace the current throttler with another throttler implementation at runtime.
 *
 * @param throttler the initial throttler implementation.
 */
public class ResettableThrottler(@Volatile private var throttler: ThreadSafeThrottler) : ThreadSafeThrottler {
    private val readWriteLock: ReentrantReadWriteLock = ReentrantReadWriteLock(true)

    override fun access(): Boolean = readWriteLock.read {
        throttler.access()
    }

    /**
     * Resets the throttling parameters by replacing the current internal throttler with a new throttler instance.
     *
     * @param throttler the new throttler implementation.
     */
    public fun reset(throttler: ThreadSafeThrottler): Unit = readWriteLock.write {
        this.throttler = throttler
    }
}
