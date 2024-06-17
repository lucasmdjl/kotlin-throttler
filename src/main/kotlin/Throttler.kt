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

import io.github.lucasmdjl.throttler.threadsafe.ThreadSafeThrottler

/**
 * An interface for a throttling mechanism that limits the rate of access to a resource or operation.
 *
 * Implementations of this interface should provide a way to control the number of accesses allowed
 * within a specified time period. The primary method, [access], determines whether a new access
 * is permitted based on the throttling policy defined by the implementation.
 *
 * Thread-safe implementations of this interface should implement [ThreadSafeThrottler] instead.
 */
public interface Throttler {
    /**
     * Grants access based on the current throttle settings.
     * @return true if access is allowed, false otherwise.
     */
    public fun access(): Boolean
}
