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

package threadsafe

import internal.TestTimeProvider
import io.github.lucasmdjl.throttler.threadsafe.SyncFixedRateThrottler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class SyncFixedRateThrottlerTest {

    @Test
    public fun init_whenCountNegative() {
        assertThrows<IllegalArgumentException> {
            SyncFixedRateThrottler(-1, 1000L)
        }
    }

    @Test
    public fun init_whenCountZero() {
        assertThrows<IllegalArgumentException> {
            SyncFixedRateThrottler(0, 1000L)
        }
    }

    @Test
    public fun init_whenMillisNegative() {
        assertThrows<IllegalArgumentException> {
            SyncFixedRateThrottler(1, -1000L)
        }
    }

    @Test
    public fun init_whenMillisZero() {
        assertThrows<IllegalArgumentException> {
            SyncFixedRateThrottler(1, 0L)
        }
    }

    @Test
    public fun init_whenCountPositiveAndMillisPositive() {
        assertDoesNotThrow {
            SyncFixedRateThrottler(1, 1000L)
        }
    }

    @Test
    public fun access_whenBelowCapacity() {
        val throttler = SyncFixedRateThrottler(5, 1000L, TestTimeProvider())
        for (i in 0..<5) {
            Assertions.assertTrue(throttler.access())
        }
    }

    @Test
    public fun access_whenAtCapacity() {
        val throttler = SyncFixedRateThrottler(5, 1000L, TestTimeProvider())
        for (i in 0..<5) {
            throttler.access()
        }
        Assertions.assertFalse(throttler.access())
    }

    @Test
    public fun access_whenAtCapacityAndFirstExpires() {
        val timeProvider = TestTimeProvider()
        val throttler = SyncFixedRateThrottler(5, 1000L, timeProvider)
        throttler.access()
        timeProvider.advance(500L)
        for (i in 0..<4) {
            throttler.access()
        }
        timeProvider.advance(500L)
        Assertions.assertTrue(throttler.access())
    }

    @Test
    public fun access_concurrentBelowCapacity() {
        val throttler = SyncFixedRateThrottler(5, 1000)
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(4)
        for (i in 0..<4) {
            Thread {
                startLatch.await()
                assertTrue(throttler.access())
                endLatch.countDown()
            }.start()
        }
        startLatch.countDown()
        endLatch.await()
        assertTrue(throttler.access())
        assertFalse(throttler.access())
    }


    @Test
    public fun access_concurrentAtCapacity() {
        val throttler = SyncFixedRateThrottler(5, 1000)
        for (i in 0..<5) {
            throttler.access()
        }
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(3)
        for (i in 0..<3) {
            Thread {
                startLatch.await()
                assertFalse(throttler.access())
                endLatch.countDown()
            }.start()
        }
        startLatch.countDown()
        endLatch.await()
    }

    @Test
    public fun access_concurrentWhenJustBelowCapacity() {
        val throttler = SyncFixedRateThrottler(5, 1000)
        for (i in 0..<4) {
            throttler.access()
        }
        val results = BooleanArray(2)
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(2)
        for (i in 0..<2) {
            Thread {
                startLatch.await()
                results[i] = throttler.access()
                endLatch.countDown()
            }.start()
        }
        startLatch.countDown()
        endLatch.await()
        assertTrue(results[0] xor results[1])
    }
}
