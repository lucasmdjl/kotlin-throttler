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
import io.github.lucasmdjl.throttler.threadsafe.BlockingThrottler
import io.github.lucasmdjl.throttler.threadsafe.NoThrottler
import io.github.lucasmdjl.throttler.threadsafe.ResettableThrottler
import io.github.lucasmdjl.throttler.threadsafe.SyncFixedRateThrottler
import io.github.lucasmdjl.throttler.threadsafe.ThreadSafeThrottler
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class ResettableThrottlerTest {

    @Test
    public fun access() {
        val innerThrottler = mockk<ThreadSafeThrottler>()
        every { innerThrottler.access() } returns true
        val throttler = ResettableThrottler(innerThrottler)
        assertTrue(throttler.access())
        verify { innerThrottler.access() }
        confirmVerified(innerThrottler)
    }

    @Test
    public fun reset() {
        val throttler = ResettableThrottler(BlockingThrottler)
        assertFalse(throttler.access())
        throttler.reset(NoThrottler)
        assertTrue(throttler.access())
    }

    @Test
    public fun reset_whenSyncFixedRateThrottler() {
        val timeProvider = TestTimeProvider()
        val throttler = ResettableThrottler(SyncFixedRateThrottler(5, 1000L, timeProvider))
        for (i in 0..<5) {
            throttler.access()
        }
        timeProvider.advance(500L)
        throttler.reset(SyncFixedRateThrottler(3, 500L, timeProvider))
        for (i in 0..<3) {
            assertTrue(throttler.access())
        }
        assertFalse(throttler.access())
    }

    @Test
    public fun access_afterConcurrentReset() {
        val throttler = ResettableThrottler(BlockingThrottler)
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(6)
        val results = BooleanArray(5)
        for (i in 0..<5) {
            Thread {
                startLatch.await()
                results[i] = throttler.access()
                endLatch.countDown()
            }.start()
        }
        Thread {
            startLatch.await()
            throttler.reset(NoThrottler)
            endLatch.countDown()
        }.start()

        startLatch.countDown()
        endLatch.await()

        assertTrue(throttler.access())
    }
}
