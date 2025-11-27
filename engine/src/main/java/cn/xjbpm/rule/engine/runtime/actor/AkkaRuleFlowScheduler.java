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
package cn.xjbpm.rule.engine.runtime.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 调度器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class AkkaRuleFlowScheduler {

    /**
     * 默认超时时间
     */
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    private final ActorSystem actorSystem;

    public AkkaRuleFlowScheduler(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    /**
     * 启动流程并同步等待结果
     *
     * @throws Exception 如果流程执行失败或超时
     */
    public void startFlow(ProcessModel ruleModel, FlowContext flowContext) throws Exception {
        // 1. 构建依赖
        NodeDependencyBuilder dependencyBuilder = new NodeDependencyBuilder();
        dependencyBuilder.buildNodeDependency(ruleModel.getChildNodes());
        // 2. 创建流程实例 Master Actor
        ActorRef masterActor = actorSystem.actorOf(WorkflowInstanceActor.props(dependencyBuilder));
        Long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        if (Objects.nonNull(ruleModel.getTimeoutSeconds())) {
            timeoutSeconds = Math.min(ruleModel.getTimeoutSeconds(), DEFAULT_TIMEOUT_SECONDS);
        }
        // 3. 总的超时设置
        Timeout timeout = new Timeout(timeoutSeconds, TimeUnit.SECONDS);
        // 4. 使用 Ask 模式发送消息
        // Patterns.ask 会返回一个 Scala Future
        Future<Object> future = Patterns.ask(
                masterActor,
                new WorkflowProtocol.StartProcess(ruleModel, flowContext),
                timeout
        );
        try {
            // 5. 同步阻塞等待结果 (Block current thread)
            // Await.result 会等待 Future 完成。
            // 如果 Actor 回复 Status.Success，这里返回 null (因为 Success 内容是 null)
            // 如果 Actor 回复 Status.Failure，这里会抛出那个异常
            Await.result(future, timeout.duration());
            log.info("流程执行成功完成: {}", ruleModel.getKey());
        } catch (TimeoutException e) {
            log.error("流程执行超时: {}", ruleModel.getKey());
            // 超时后可以考虑停止那个 Actor，防止它还在后台跑
            actorSystem.stop(masterActor);
            throw e;
        } catch (Exception e) {
            log.error("流程执行失败: {}", e.getMessage());
            throw e;
        }
    }
}