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
package cn.xjbpm.rule.utils;

import cn.xjbpm.rule.node.enums.TimeUnit;
import cn.xjbpm.rule.node.model.TriggerRule;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/9
 */
public class RuleParserUtil {

    /**
     * 将 TriggerRule 对象解析为标准的 Cron 表达式。
     * Cron 格式：秒 分 时 日 月 周
     *
     * @param rule 数据库中读取的触发规则
     * @return Cron 表达式字符串
     */
    public static String generateCronExpression(TriggerRule rule) {
        if (rule == null) {
            return null;
        }
        TimeUnit unit = rule.getUnit();
        Integer interval = rule.getIntervalValue();

        // 确保时和分不为空
        Integer hour = rule.getAtHour() != null ? rule.getAtHour() : 0;
        Integer minute = rule.getAtMinute() != null ? rule.getAtMinute() : 0;

        // 1. Cron 模式 (保持不变)
        if (unit == TimeUnit.CRON && rule.getCronExpression() != null) {
            if (rule.getCronExpression().split(" ").length == 5) {
                return "0 " + rule.getCronExpression();
            }
            return rule.getCronExpression();
        }

        // 2. 间隔模式：生成基于间隔的表达式
        if (interval == null || interval <= 0) {
            throw new IllegalArgumentException("间隔数值必须大于 0");
        }

        String minutePart = minute.toString();
        String hourPart = hour.toString();

        // Cron 格式: 秒(0-59) 分(0-59) 时(0-23) 日(1-31) 月(1-12) 周(1-7或MON-SUN)

        switch (unit) {
            case SECONDS:
                // 每隔 X 秒执行: 0/X * * * * ?
                return String.format("0/%d * * * * ?", interval);

            case MINUTES:
                // 每隔 X 分钟执行: 0 0/X * * * ?
                // 使用 atMinute 确保精度：从指定分钟开始，每隔 X 分钟
                return String.format("0 %s/%d * * * ?", minutePart, interval);

            case HOURS:
                // 修正：从指定分钟开始计时，每隔 X 小时，使用 atMinute 确保精度
                // 0 [minute] 0/X * * ?
                return String.format("0 %s 0/%d * * ?", minutePart, interval);

            case DAYS:
                // 每隔 X 天，在指定时间执行: 0 分 时 1/X * ?
                // 注意：当使用了日字段(1/X)时，周字段必须是 '?'
                String dayOfMonthInterval = String.format("1/%d", interval);
                return String.format("0 %s %s %s * ?", minutePart, hourPart, dayOfMonthInterval);

            case WEEKS:
                // 每周 X N时执行 (忽略 intervalValue > 1)
                Integer dayOfWeek = rule.getAtDayOfWeek();
                if (dayOfWeek == null) {
                    // 如果用户没有指定，则默认周日 (1)
                    dayOfWeek = 1;
                }
                // 必须使用 '?' 替代日字段，避免冲突
                String dayOfWeekPart = dayOfWeek.toString();
                return String.format("0 %s %s ? * %s", minutePart, hourPart, dayOfWeekPart);

            case MONTHS:
                // 每隔 X 月，在每月 1 号指定时间执行: 0 分 时 1 1/X ?
                String monthInterval = String.format("1/%d", interval);
                String dayOfMonth = "1"; // 每月 1 号
                // 注意：周字段必须是 '?'
                return String.format("0 %s %s %s %s ?", minutePart, hourPart, dayOfMonth, monthInterval);

            default:
                throw new UnsupportedOperationException("不受支持的 TimeUnit: " + unit);
        }
    }
}
