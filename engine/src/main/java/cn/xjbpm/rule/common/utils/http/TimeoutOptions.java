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

package cn.xjbpm.rule.common.utils.http;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public final class TimeoutOptions {

    public static final TimeoutOptions DEFAULT = new TimeoutOptions(3000, 5000);
    public static final TimeoutOptions FAST = new TimeoutOptions(1000, 3000);
    public static final TimeoutOptions SLOW = new TimeoutOptions(5000, Long.valueOf(TimeUnit.MINUTES.toMillis(1L)).intValue());
    public static final TimeoutOptions VERAY_SLOW = new TimeoutOptions(10000, Long.valueOf(TimeUnit.MINUTES.toMillis(10L)).intValue());
    private final int connectTimeout;
    private final int readTimeout;

    @Getter
    @Setter
    private String proxyHost;

    public TimeoutOptions(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public TimeoutOptions(int connectTimeout, int readTimeout, String proxyHost) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.proxyHost = proxyHost;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public TimeoutOptions copy() {
        return new TimeoutOptions(connectTimeout, readTimeout, proxyHost);
    }
}