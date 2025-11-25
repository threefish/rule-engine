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
package cn.xjbpm.rule.engine.runtime.model;

import cn.xjbpm.rule.common.utils.TimeFormatUtil;
import lombok.Builder;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/21
 */
@Data
@Builder
public class NodeExcution {
    private String id;
    private String name;
    private String timeConsuming;
    private long startTime;
    private long endTime;
    private Boolean conditionsMeet;
    private ExecutStatus status;
    private String errorMessage;

    public String getTimeConsuming() {
        return TimeFormatUtil.formatNanosToMs(endTime - startTime);
    }

}