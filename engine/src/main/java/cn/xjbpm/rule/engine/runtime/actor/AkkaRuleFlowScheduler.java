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
    private final long DEFAULT_TIMEOUT_SECONDS;

    private final ActorSystem actorSystem;
    /**
     * 全局共享路由
     */
    private final ActorRef globalWorkerRouter;

    public AkkaRuleFlowScheduler(ActorSystem actorSystem, long akkaDefaultTimeoutSeconds, int akkaGlobalWorkerPoolSize) {
        this.actorSystem = actorSystem;
        this.DEFAULT_TIMEOUT_SECONDS = akkaDefaultTimeoutSeconds;
        // 初始化全局路由池
        this.globalWorkerRouter = actorSystem.actorOf(new RoundRobinPool(akkaGlobalWorkerPoolSize).props(NodeWorkerActor.props()), "global-worker-router");
        log.info("AkkaRuleFlowScheduler 初始化完成，全局路由池大小: {}", akkaGlobalWorkerPoolSize);
    }

    /**
     * 启动流程并同步等待结果
     */
    public void startFlow(RuleFlowModel ruleModel, FlowContext flowContext) throws Exception {
        startFlow(ruleModel, flowContext, Collections.emptySet());
    }

    public void startFlow(RuleFlowModel ruleModel, FlowContext flowContext, Set<String> skipNodeIds) throws Exception {
        ActorRef masterActor = actorSystem.actorOf(
                WorkflowInstanceActor.props(new NodeDependencyBuilder(ruleModel.getChildNodes()), globalWorkerRouter)
        );
        Timeout timeout = calculateTimeout(ruleModel);
        Future<Object> future = Patterns.ask(masterActor, new WorkflowProtocol.StartProcess(ruleModel, flowContext, skipNodeIds), timeout);
        try {
            Await.result(future, timeout.duration());
            log.info("流程执行成功完成: {}", ruleModel.getKey());
        } catch (TimeoutException e) {
            log.error("流程执行超时: {}", ruleModel.getKey());
            actorSystem.stop(masterActor);
            throw e;
        } catch (Exception e) {
            log.error("流程执行失败: {}", e.getMessage());
            throw e;
        }
    }

    private Timeout calculateTimeout(RuleFlowModel ruleModel) {
        Long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        if (Objects.nonNull(ruleModel.getTimeoutSeconds())) {
            timeoutSeconds = Math.min(ruleModel.getTimeoutSeconds(), DEFAULT_TIMEOUT_SECONDS);
        }
        return new Timeout(timeoutSeconds, TimeUnit.SECONDS);
    }

    public void startFlowAsync(RuleFlowModel ruleModel, FlowContext flowContext, FlowOnComplete onCompletion) {
        startFlowAsync(ruleModel, flowContext, Collections.emptySet(), onCompletion);
    }

    public void startFlowAsync(RuleFlowModel ruleModel, FlowContext flowContext, Set<String> skipNodeIds, FlowOnComplete onCompletion) {
        ActorRef masterActor = actorSystem.actorOf(
                WorkflowInstanceActor.props(new NodeDependencyBuilder(ruleModel.getChildNodes()), globalWorkerRouter)
        );
        Timeout timeout = calculateTimeout(ruleModel);
        Future<Object> scalaFuture = Patterns.ask(masterActor, new WorkflowProtocol.StartProcess(ruleModel, flowContext, skipNodeIds), timeout);
        scalaFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable failure, Object success) {
                if (failure != null) {
                    log.error("流程异步执行异常: {}", ruleModel.getKey(), failure);
                    if (failure instanceof TimeoutException) {
                        actorSystem.stop(masterActor);
                    }
                } else {
                    log.info("流程异步执行完成: {}", ruleModel.getKey());
                }
                if (onCompletion != null) {
                    try {
                        onCompletion.onComplete(failure);
                    } catch (Exception e) {
                        log.error("流程完成回调执行异常: {}", ruleModel.getKey(), e);
                    }
                }
            }
        }, actorSystem.dispatcher());
    }
}