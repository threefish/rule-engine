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

package cn.xjbpm.rule.engine.runtime.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.routing.DefaultResizer;
import akka.routing.RoundRobinPool;
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
import java.util.function.Consumer;

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
    private final long defaultExecuteTimeoutSeconds;

    private final ActorSystem actorSystem;

    /**
     * 全局共享路由
     */
    private final ActorRef globalWorkerRouter;

    /**
     * 调试事件发布回调，由 service 层注入，引擎本身不感知 Spring。
     */
    private final Consumer<FlowContext> debugPublisher;

    public AkkaRuleFlowScheduler(ActorSystem actorSystem, long akkaDefaultExecuteTimeoutSeconds,
                                 int akkaGlobalWorkerPoolInitSize, int akkaGlobalWorkerPoolMaxSize,
                                 Consumer<FlowContext> debugPublisher) {
        this.actorSystem = actorSystem;
        this.defaultExecuteTimeoutSeconds = akkaDefaultExecuteTimeoutSeconds;
        this.debugPublisher = debugPublisher;
        // 初始化全局路由池，将 debugPublisher 注入每个 Worker
        this.globalWorkerRouter = actorSystem.actorOf(
                new RoundRobinPool(0)
                        .withResizer(new DefaultResizer(akkaGlobalWorkerPoolInitSize, akkaGlobalWorkerPoolMaxSize))
                        .props(NodeWorkerActor.props(debugPublisher)), "akka-global-worker-router");
        log.info("AkkaRuleFlowScheduler 初始化完成，全局路由池大小: init:{} max:{}", akkaGlobalWorkerPoolInitSize, akkaGlobalWorkerPoolMaxSize);
    }

    /**
     * 启动规则流并同步等待结果
     */
    public void startFlow(RuleFlowModel ruleModel, FlowContext flowContext) throws Exception {
        startFlow(ruleModel, flowContext, Collections.emptySet());
    }

    public void startFlow(RuleFlowModel ruleModel, FlowContext flowContext, Set<String> skipNodeIds) throws Exception {
        ActorRef masterActor = actorSystem.actorOf(
                WorkflowInstanceActor.props(new NodeDependencyBuilder(ruleModel.getChildNodes()), globalWorkerRouter, debugPublisher)
        );
        Timeout timeout = calculateTimeout(ruleModel);
        Future<Object> future = Patterns.ask(masterActor, new WorkflowProtocol.StartProcess(ruleModel, flowContext, skipNodeIds), timeout);
        try {
            Await.result(future, timeout.duration());
            log.info("规则流执行成功完成: {}", ruleModel.getKey());
        } catch (TimeoutException e) {
            log.error("规则流执行超时: {}", ruleModel.getKey());
            actorSystem.stop(masterActor);
            throw e;
        } catch (Exception e) {
            log.error("规则流执行失败: {}", e.getMessage());
            throw e;
        }
    }

    private Timeout calculateTimeout(RuleFlowModel ruleModel) {
        Long timeoutSeconds = defaultExecuteTimeoutSeconds;
        if (Objects.nonNull(ruleModel.getTimeoutSeconds())) {
            timeoutSeconds = Math.min(ruleModel.getTimeoutSeconds(), defaultExecuteTimeoutSeconds);
        }
        return new Timeout(timeoutSeconds, TimeUnit.SECONDS);
    }

    public void startFlowAsync(RuleFlowModel ruleModel, FlowContext flowContext, FlowOnComplete onCompletion) {
        startFlowAsync(ruleModel, flowContext, Collections.emptySet(), onCompletion);
    }

    public void startFlowAsync(RuleFlowModel ruleModel, FlowContext flowContext, Set<String> skipNodeIds, FlowOnComplete onCompletion) {
        ActorRef masterActor = actorSystem.actorOf(
                WorkflowInstanceActor.props(new NodeDependencyBuilder(ruleModel.getChildNodes()), globalWorkerRouter, debugPublisher)
        );
        Timeout timeout = calculateTimeout(ruleModel);
        Future<Object> scalaFuture = Patterns.ask(masterActor, new WorkflowProtocol.StartProcess(ruleModel, flowContext, skipNodeIds), timeout);
        scalaFuture.onComplete(new OnComplete<>() {
            @Override
            public void onComplete(Throwable failure, Object success) {
                if (failure != null) {
                    log.error("规则流异步执行异常: {}", ruleModel.getKey(), failure);
                    if (failure instanceof TimeoutException) {
                        actorSystem.stop(masterActor);
                    }
                } else {
                    log.info("规则流异步执行完成: {}", ruleModel.getKey());
                }
                if (onCompletion != null) {
                    try {
                        onCompletion.onComplete(failure);
                    } catch (Exception e) {
                        log.error("规则流完成回调执行异常: {}", ruleModel.getKey(), e);
                    }
                }
            }
        }, actorSystem.dispatcher());
    }
}
