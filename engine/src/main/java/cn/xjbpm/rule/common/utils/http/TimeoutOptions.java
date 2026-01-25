/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.common.utils.http;

import java.util.concurrent.TimeUnit;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public final class TimeoutOptions {

    public static final TimeoutOptions DEFAULT = new TimeoutOptions(3000, 5000);
    public static final TimeoutOptions FAST = new TimeoutOptions(1000, 2000);
    public static final TimeoutOptions SLOW = new TimeoutOptions(5000, Long.valueOf(TimeUnit.SECONDS.toMillis(15L)).intValue());
    public static final TimeoutOptions VERAY_SLOW = new TimeoutOptions(5000, Long.valueOf(TimeUnit.MINUTES.toMillis(10L)).intValue());
    private final int connectTimeout;
    private final int readTimeout;

    public TimeoutOptions(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }
}
