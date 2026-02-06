/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.xjbpm.rule.common.utils;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class TimeFormatUtil {

    // 定义纳秒显示的最大阈值（4位数字），超过此值即切换为ms
    private static final long MAX_NS_DISPLAY_THRESHOLD = 10_000; // 10,000 ns

    // 1 秒 = 1000 ms
    private static final long MS_TO_S_THRESHOLD = 1_000; // 1000 ms

    public static String formatNanosToMs(long nanoseconds) {
        if (nanoseconds < 0) {
            return "0ms";
        }
        // 1. 如果小于 10,000 纳秒，直接显示为纳秒 (最多4位数字)
        if (nanoseconds < MAX_NS_DISPLAY_THRESHOLD) {
            return nanoseconds + "ns";
        }
        double milliseconds = nanoseconds / 1_000_000.0;
        // 2. 小于 1000 ms，显示 ms
        if (milliseconds < MS_TO_S_THRESHOLD) {
            return String.format("%.2fms", milliseconds);
        }
        // 3. 大于等于 1000 ms，显示 s
        double seconds = milliseconds / 1_000.0;
        return String.format("%.2fs", seconds);
    }

    public static String formatMs(long millis) {
        if (millis <= 0) {
            return "0ms";
        }

        // 小于 1 秒，直接显示毫秒
        if (millis < 1_000) {
            return millis + "ms";
        }

        // 大于等于 1 秒，显示秒
        double seconds = millis / 1_000.0;
        return String.format("%.2fs", seconds);
    }

}