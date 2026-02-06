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

package cn.xjbpm.rule.node.model;

import cn.xjbpm.rule.node.enums.TimeUnit;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/9
 */
@Data
public class TriggerRule {

    private TimeUnit unit;

    // 适用于间隔和日期模式
    private Integer intervalValue; // 间隔数值 (e.g., 5, 1)
    private Integer atHour;        // 执行小时 (0-23)
    private Integer atMinute;      // 执行分钟 (0-59)
    private Integer atDayOfWeek;   // 周几

    // 适用于 Cron 模式
    private String cronExpression; // Cron 表达式
}