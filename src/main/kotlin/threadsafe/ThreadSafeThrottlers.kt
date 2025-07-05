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

package dev.lucasmdjl.throttler.threadsafe

import dev.lucasmdjl.throttler.FixedRateThrottler
import dev.lucasmdjl.throttler.internal.SystemClock
import dev.lucasmdjl.throttler.internal.Clock
import kotlin.concurrent.Volatile


/**
 * A thread-safe throttler based on a specified number of allowed accesses within a given time period.
 *
 * @param count the maximum number of accesses allowed within the time period. Must be positive.
 * @param millis the time period in milliseconds. Must be positive.
 */
public class SyncFixedRateThrottler internal constructor(count: Int, millis: Long, clock: Clock) : ThreadSafeThrottler {
    public constructor(count: Int, millis: Long) : this(count, millis, SystemClock)
    private val unsafeFixedRateThrottler = FixedRateThrottler(count, millis, clock)

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
 * A thread-safe throttler that allows dynamic resetting of its internal throttler implementation.
 *
 * Note: While concurrent calls to `reset` and `access` may result in race conditions,
 * these races are benign and do not affect the correctness of the program.
 * The result of `access` may reflect either the old or new throttler depending on the timing of the operations,
 * which is acceptable for the intended use case, as this would be the result if `access` ran just before
 * or just after `reset`, respectively.
 *
 * This class is not strictly thread-safe but is safe for concurrent use.
 */
public class ResettableThrottler(@Volatile private var throttler: ThreadSafeThrottler) : ThreadSafeThrottler {

    override fun access(): Boolean = throttler.access()

    /**
     * Resets the throttling parameters by replacing the current internal throttler with a new throttler instance.
     *
     * @param throttler the new throttler implementation.
     */
    public fun reset(throttler: ThreadSafeThrottler) {
        this.throttler = throttler
    }
}
