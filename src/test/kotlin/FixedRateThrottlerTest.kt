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

import internal.TestTimeProvider
import io.github.lucasmdjl.throttler.FixedRateThrottler
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test


public class FixedRateThrottlerTest {

    @Test
    public fun init_whenCountNegative() {
        assertThrows<IllegalArgumentException> {
            FixedRateThrottler(-1, 1000L)
        }
    }

    @Test
    public fun init_whenCountZero() {
        assertThrows<IllegalArgumentException> {
            FixedRateThrottler(0, 1000L)
        }
    }

    @Test
    public fun init_whenMillisNegative() {
        assertThrows<IllegalArgumentException> {
            FixedRateThrottler(1, -1000L)
        }
    }

    @Test
    public fun init_whenMillisZero() {
        assertThrows<IllegalArgumentException> {
            FixedRateThrottler(1, 0L)
        }
    }

    @Test
    public fun init_whenCountPositiveAndMillisPositive() {
        assertDoesNotThrow {
            FixedRateThrottler(1, 1000L)
        }
    }

    @Test
    public fun access_whenBelowCapacity() {
        val throttler = FixedRateThrottler(5, 1000L, TestTimeProvider())
        for (i in 0..<5) {
            assertTrue(throttler.access())
        }
    }

    @Test
    public fun access_whenAtCapacity() {
        val throttler = FixedRateThrottler(5, 1000L, TestTimeProvider())
        for (i in 0..<5) {
            throttler.access()
        }
        assertFalse(throttler.access())
    }

    @Test
    public fun access_whenAtCapacityAndFirstExpires() {
        val timeProvider = TestTimeProvider()
        val throttler = FixedRateThrottler(5, 1000L, timeProvider)
        throttler.access()
        timeProvider.advance(500L)
        for (i in 0..<4) {
            throttler.access()
        }
        timeProvider.advance(500L)
        assertTrue(throttler.access())
    }

    @Test
    public fun access_whenAtCapacityAndFirstExpiresAndAtCapacityAgain() {
        val timeProvider = TestTimeProvider()
        val throttler = FixedRateThrottler(5, 1000L, timeProvider)
        throttler.access()
        timeProvider.advance(500L)
        for (i in 0..<4) {
            throttler.access()
        }
        timeProvider.advance(500L)
        throttler.access()
        assertFalse(throttler.access())
    }
}
