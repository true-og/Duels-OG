/*
 * Derived from the EternalCombat (https://github.com/EternalCodeTeam/EternalCombat)
 * BorderPoint record by the EternalCode Team, originally licensed under the Apache License,
 * Version 2.0.
 *
 * Modifications by the TrueOG Network for Duels-OG:
 *   - Converted from a Java 14 record to a Java 8 final class.
 *   - Dropped the nullable innerPoint field; Duels' border renderer does not use the
 *     chunk-edge thickness adjustment that EternalCombat relied on for its packet batching.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package me.realized.duels.arena.border;

final class BorderPoint {

    private final int x;
    private final int y;
    private final int z;

    BorderPoint(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    int x() {
        return x;
    }

    int y() {
        return y;
    }

    int z() {
        return z;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (!(other instanceof BorderPoint)) { return false; }
        final BorderPoint o = (BorderPoint) other;
        return x == o.x && y == o.y && z == o.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}
