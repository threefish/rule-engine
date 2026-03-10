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

package cn.xjbpm.rule.service;

import cn.xjbpm.rule.job.DynamicJob;
import cn.xjbpm.rule.repository.entity.RuleFlowScheduledEntity;
import cn.xjbpm.rule.utils.FieldUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Service
@Slf4j
@AllArgsConstructor
public class SchedulerService {

    private final Scheduler scheduler;

    /**
     * 扩展功能：重置指定 ruleFlowKey 的所有任务（先删后加）
     * 通常业务更新时调用此方法
     */
    public void refreshRuleFlowTasks(String ruleFlowKey, List<RuleFlowScheduledEntity> newTasks) {
        // 1. 先清理
        clearScheduledTasks(ruleFlowKey);
        // 2. 再添加
        addScheduledTasks(newTasks);
    }

    /**
     * 1. 指定 ruleFlowKey 清除相关的定时任务
     * 利用 Quartz 的 Group 机制，将 ruleFlowKey 作为 GroupName，一次性查找并删除
     *
     * @param ruleFlowKey 规则流唯一标识
     */
    public void clearScheduledTasks(String ruleFlowKey) {
        try {
            // 匹配该组下的所有 Job
            GroupMatcher<JobKey> matcher = GroupMatcher.jobGroupEquals(ruleFlowKey);
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            if (jobKeys != null && !jobKeys.isEmpty()) {
                // 批量删除
                scheduler.deleteJobs(new ArrayList<>(jobKeys));
                log.info("已清除 ruleFlowKey=[{}] 下的 {} 个定时任务", ruleFlowKey, jobKeys.size());
            }
        } catch (SchedulerException e) {
            log.error("清除定时任务失败 ruleFlowKey={}", ruleFlowKey, e);
        }
    }

    /**
     * 2. 给与一个实体列表添加一批定时任务
     *
     * @param taskList 定时任务实体列表
     */
    public void addScheduledTasks(List<RuleFlowScheduledEntity> taskList) {
        if (taskList == null || taskList.isEmpty()) {
            return;
        }
        for (RuleFlowScheduledEntity task : taskList) {
            try {
                // 校验 Cron 表达式是否有效
                if (!CronExpression.isValidExpression(task.getCronExpression())) {
                    log.error("Cron表达式无效，跳过调度: id={}, cron={}", task.getId(), task.getCronExpression());
                    continue;
                }
                // 构建任务
                scheduleJob(task);
            } catch (Exception e) {
                log.error("调度任务失败: id={}, ruleFlowKey={}", task.getId(), task.getRuleFlowKey(), e);
            }
        }
    }

    /**
     * 辅助方法：构建并调度单个任务
     */
    private void scheduleJob(RuleFlowScheduledEntity task) throws SchedulerException {
        // 策略：
        // JobName = "JOB_" + 数据库主键ID (保证唯一)
        // GroupName = ruleFlowKey (便于按组管理)
        String jobId = "JOB_" + task.getId();
        String groupName = task.getRuleFlowKey(); // 关键：使用 Key 作为组名

        // 1. 创建 JobDetail
        JobDetail jobDetail = JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobId, groupName)
                .usingJobData(FieldUtil.name(RuleFlowScheduledEntity::getId), task.getId())
                .usingJobData(FieldUtil.name(RuleFlowScheduledEntity::getRuleFlowKey), task.getRuleFlowKey())
                .usingJobData(FieldUtil.name(RuleFlowScheduledEntity::getCronExpression), task.getCronExpression())
                .usingJobData(FieldUtil.name(RuleFlowScheduledEntity::getRequestParams), task.getRequestParams())
                .build();

        // 2. 创建 CronTrigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("TRIGGER_" + task.getId(), groupName)
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing()) // 错过触发策略
                .build();

        // 3. 注册到调度器
        scheduler.scheduleJob(jobDetail, trigger);
        log.info("成功添加定时任务: id={}, group={}, cron={}", task.getId(), groupName, task.getCronExpression());
    }
}