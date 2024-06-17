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

package io.github.lucasmdjl.throttler

import io.github.lucasmdjl.fixedqueuecapacity.FixedCapacityLongArrayQueue
import io.github.lucasmdjl.throttler.internal.SystemTimeProvider
import io.github.lucasmdjl.throttler.internal.TimeProvider

/**
 * A throttler based on a specified number of allowed accesses within a given time period.
 * This class is not thread-safe. Concurrent calls to [access] will cause data races.
 *
 * @param count the maximum number of accesses allowed within the time period. Must be positive.
 * @param millis the time period in milliseconds. Must be positive.
 */
public class FixedRateThrottler internal constructor(
    count: Int, private val millis: Long, private val timeProvider: TimeProvider
) : Throttler {
    public constructor(count: Int, millis: Long) : this(count, millis, SystemTimeProvider)
    init {
        require(count > 0) { "count must be positive." }
        require(millis > 0) { "millis must be positive." }
    }

    private val accesses: FixedCapacityLongArrayQueue = FixedCapacityLongArrayQueue(count)

    override fun access(): Boolean {
        val now = timeProvider.currentTimeMillis()
        cleanUp(now)
        return accesses.offer(now)
    }

    /**
     * Cleans up old access records that are outside the allowed time window.
     *
     * @param now the current time in milliseconds.
     */
    private fun cleanUp(now: Long) {
        do {
            val access = accesses.pollIf { now - it >= millis }
        } while (access != null)
    }
}
