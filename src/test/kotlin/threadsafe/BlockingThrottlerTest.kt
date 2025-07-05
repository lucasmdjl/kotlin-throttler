/*
 *
 * Kotlin Throttler: Kotlin micro-library for throttling.
 * Copyright (C) 2025 Lucas M. de Jong Larrarte
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package threadsafe

import dev.lucasmdjl.throttler.threadsafe.BlockingThrottler
import kotlin.test.Test
import kotlin.test.assertFalse

public class BlockingThrottlerTest {
    @Test
    public fun access() {
        val throttler = BlockingThrottler
        assertFalse(throttler.access())
    }
}
