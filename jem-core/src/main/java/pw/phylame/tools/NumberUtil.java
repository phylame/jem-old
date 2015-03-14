/*
 * Copyright 2015 Peng Wan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.tools;

/**
 * Utility class for number operations.
 */
public abstract class NumberUtil {

    private static java.util.Random random = null;

    public static int randInteger(int bottom, int top) {
        if (random == null) {
            random = new java.util.Random(System.currentTimeMillis());
        }
        return random.nextInt(top-bottom) + bottom;
    }

    public static Long randLong(long bottom, long top) {
        if (random == null) {
            random = new java.util.Random(System.currentTimeMillis());
        }
        return random.nextInt((int) (top-bottom)) + bottom;
    }
}
