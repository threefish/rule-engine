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

package cn.xjbpm.rule.engine.runtime.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/21
 */
@Data
@NoArgsConstructor
public class TraceLog {

    private long time;
    private String nodeId;
    private String message;

    public TraceLog(long time, String nodeId, String message) {
        this.nodeId = nodeId;
        this.time = time;
        this.message = message;
    }

    public static TraceLog of(String message) {
        return new TraceLog(System.nanoTime(), null, message);
    }

    public static TraceLog of(String nodeId, String message) {
        return new TraceLog(System.nanoTime(), nodeId, message);
    }
}