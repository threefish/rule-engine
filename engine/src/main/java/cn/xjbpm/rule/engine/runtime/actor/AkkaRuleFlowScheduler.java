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
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
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
     * @param ruleModel   流程模型
     * @param flowContext 流程上下文
     * @throws Exception 如果流程执行失败或超时
     */
    public void startFlow(RuleFlowModel ruleModel, FlowContext flowContext) throws Exception {
        startFlow(ruleModel, flowContext, Collections.emptySet());
    }

    /**
     * 启动流程并同步等待结果
     *
     * @param ruleModel   流程模型
     * @param flowContext 流程上下文
     * @param skipNodeIds 已经执行完成的节点ID列表
     * @throws Exception 如果流程执行失败或超时
     */
    public void startFlow(RuleFlowModel ruleModel, FlowContext flowContext, Set<String> skipNodeIds) throws Exception {
        // 1. 构建依赖
        NodeDependencyBuilder dependencyBuilder = new NodeDependencyBuilder(ruleModel.getChildNodes());
        // 2. 创建流程实例 Master Actor
        ActorRef masterActor = actorSystem.actorOf(WorkflowInstanceActor.props(dependencyBuilder));
        // 3. 总的超时设置
        Timeout timeout = calculateTimeout(ruleModel);
        // 4. 使用 Ask 模式发送消息
        // Patterns.ask 会返回一个 Scala Future
        Future<Object> future = Patterns.ask(masterActor, new WorkflowProtocol.StartProcess(ruleModel, flowContext, skipNodeIds), timeout);
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

    /**
     * 计算超时时间
     *
     * @param ruleModel
     * @return
     */

    private Timeout calculateTimeout(RuleFlowModel ruleModel) {
        Long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        if (Objects.nonNull(ruleModel.getTimeoutSeconds())) {
            timeoutSeconds = Math.min(ruleModel.getTimeoutSeconds(), DEFAULT_TIMEOUT_SECONDS);
        }
        return new Timeout(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 异步启动流程，并在流程结束后（无论成功或失败）执行指定的回调函数。
     *
     * @param ruleModel   流程模型
     * @param flowContext 流程上下文
     */
    public void startFlowAsync(RuleFlowModel ruleModel, FlowContext flowContext, FlowOnComplete onCompletion) {
        startFlowAsync(ruleModel, flowContext, Collections.emptySet(), onCompletion);
    }

    /**
     * 异步启动流程，并在流程结束后（无论成功或失败）执行指定的回调函数。
     *
     * @param ruleModel    流程模型
     * @param flowContext  流程上下文
     * @param skipNodeIds  跳过节点列表 (跳过模式)
     * @param onCompletion 流程结束时执行的回调（Runnable），不接收结果或异常，只表示流程已终止。
     */
    public void startFlowAsync(RuleFlowModel ruleModel, FlowContext flowContext, Set<String> skipNodeIds, FlowOnComplete onCompletion) {
        // 1. 构建依赖
        NodeDependencyBuilder dependencyBuilder = new NodeDependencyBuilder(ruleModel.getChildNodes());
        // 2. 创建流程实例 Master Actor
        ActorRef masterActor = actorSystem.actorOf(WorkflowInstanceActor.props(dependencyBuilder));
        Timeout timeout = calculateTimeout(ruleModel);
        // 3. 发送消息获取 Scala Future
        Future<Object> scalaFuture = Patterns.ask(masterActor, new WorkflowProtocol.StartProcess(ruleModel, flowContext, skipNodeIds), timeout);
        // 4. 附加回调
        scalaFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable failure, Object success) {
                // 1. 处理流程自身的日志和清理工作
                if (failure != null) {
                    log.error("流程异步执行异常: {}", ruleModel.getKey(), failure);
                    // 如果是超时异常，需要手动停止 Actor (防止僵尸 Actor)
                    if (failure instanceof TimeoutException) {
                        actorSystem.stop(masterActor);
                    }
                } else {
                    log.info("流程异步执行完成: {}", ruleModel.getKey());
                }
                // 2. 执行用户指定的回调方法
                if (onCompletion != null) {
                    try {
                        // 回调方法将在 Akka Dispatcher 线程中执行
                        onCompletion.onComplete(failure);
                    } catch (Exception e) {
                        // 捕获回调方法本身的异常，不影响主流程的日志
                        log.error("流程完成回调执行异常: {}", ruleModel.getKey(), e);
                    }
                }
            }
        }, actorSystem.dispatcher());
    }

}