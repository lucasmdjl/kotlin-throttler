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

package dev.lucasmdjl.throttler.internal

/**
 * An interface for providing the current time in milliseconds.
 */
internal interface Clock {
    /**
     * @return the current time according to this time provider in milliseconds.
     */
    fun currentTimeMillis(): Long
}

/**
 * A system-based implementation of [Clock] that returns the current time in milliseconds.
 */
internal object SystemClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
