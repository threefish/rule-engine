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

package cn.xjbpm.rule.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.NamedThreadLocal;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 事务延迟操作持有者：确保操作在 Spring 事务提交成功后执行。
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class TransactionOptDelayerHolder {

    /***
     * 事务线程对应延迟操作事件绑定。使用 Map 结构以支持根据事务名称区分，
     * 但主要用于避免在多层事务中，低层事务清除不该清除的队列。
     */
    private static final ThreadLocal<Map<String, BlockingQueue<TransactionDelayEvent>>> AFTER_TRANSACTION_OPTS_THREAD_LOCAL =
            new NamedThreadLocal<>("事务交易成功后执行操作");

    /**
     * 事务提交成功后执行
     *
     * @param event 待执行的事件
     */
    public static void executeAfterTransactionCommit(TransactionDelayEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 当前操作在活跃的事务中
            // 注册事务同步监听器，确保只注册一次
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                // 如果同步未激活，通常意味着这是第一个注册的操作，需要激活同步
                TransactionSynchronizationManager.initSynchronization();
            }
            TransactionSynchronizationManager.registerSynchronization(AfterTransactionSynchronizationAdapter.INSTANCE);
            BlockingQueue<TransactionDelayEvent> eventExecutables = getExecutablesCreateIfNecessary();
            eventExecutables.add(event);
        } else {
            throw new RuntimeException("事务后置操作必须在一个活跃的事务中");
        }
    }

    /**
     * 从 ThreadLocal 获取执行器队列, 如果没有拿到, 就创建一个。
     * 使用事务名称区分队列。
     *
     * @return 延迟事件队列
     */
    private static BlockingQueue<TransactionDelayEvent> getExecutablesCreateIfNecessary() {
        Map<String, BlockingQueue<TransactionDelayEvent>> eventExecutableMap = AFTER_TRANSACTION_OPTS_THREAD_LOCAL.get();
        String transactionName = getTransactionName();
        if (eventExecutableMap == null) {
            eventExecutableMap = new HashMap<>();
            AFTER_TRANSACTION_OPTS_THREAD_LOCAL.set(eventExecutableMap);
        }
        BlockingQueue<TransactionDelayEvent> executables = eventExecutableMap.get(transactionName);
        if (executables == null) {
            executables = new LinkedBlockingQueue<>();
            eventExecutableMap.put(transactionName, executables);
        }
        return executables;
    }

    /**
     * 获取当前事务名称
     * Spring 事务默认为全类名加方法名，编程事务可能为 null。
     *
     * @return 事务名称
     */
    private static String getTransactionName() {
        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        if (StringUtils.isBlank(transactionName)) {
            // 统一非编程式事务的默认名称
            return "default-transaction-name";
        }
        return transactionName;
    }

    public interface TransactionDelayEvent {
        /**
         * 执行事件
         */
        void onEvent();
    }

    /**
     * 事务同步回调适配器
     * 事务提交之后,执行延迟操作
     */
    @Slf4j
    private final static class AfterTransactionSynchronizationAdapter implements TransactionSynchronization {

        // 使用单例模式
        public static final AfterTransactionSynchronizationAdapter INSTANCE = new AfterTransactionSynchronizationAdapter();

        private AfterTransactionSynchronizationAdapter() {
        }

        /**
         * 事务提交成功后，执行绑定的延迟操作。
         */
        @Override
        public void afterCommit() {
            Map<String, BlockingQueue<TransactionDelayEvent>> blockingQueueMap = AFTER_TRANSACTION_OPTS_THREAD_LOCAL.get();
            String transactionName = getTransactionName();

            // 安全检查
            if (blockingQueueMap == null) {
                log.warn("afterCommit 阶段发现 ThreadLocal Map 为空，可能存在并发或清理问题。");
                return;
            }

            BlockingQueue<TransactionDelayEvent> eventExecutables = blockingQueueMap.get(transactionName);

            if (Objects.nonNull(eventExecutables) && !eventExecutables.isEmpty()) {
                while (!eventExecutables.isEmpty()) {
                    TransactionDelayEvent eventExecutable = eventExecutables.poll();
                    if (Objects.nonNull(eventExecutable)) {
                        try {
                            // 💡 警告：执行事务后置操作，需要确保 onEvent() 具备重试和错误处理能力
                            eventExecutable.onEvent();
                        } catch (Throwable e) {
                            // 记录详细的错误日志
                            log.error("执行事务后置操作出错, transactionName={}", transactionName, e);
                        }
                    }
                }
            }
        }

        /**
         * 事务完成之后(包括回滚和提交)，清除绑定的操作。
         *
         * @param status 事务状态
         */
        @Override
        public void afterCompletion(int status) {
            Map<String, BlockingQueue<TransactionDelayEvent>> map = AFTER_TRANSACTION_OPTS_THREAD_LOCAL.get();
            if (map != null) {
                String transactionName = getTransactionName();
                // 移除当前事务的队列
                map.remove(transactionName);
                // 如果 Map 为空，移除整个 ThreadLocal 引用
                if (map.isEmpty()) {
                    AFTER_TRANSACTION_OPTS_THREAD_LOCAL.remove();
                    if (log.isDebugEnabled()) {
                        log.debug("线程事务执行完毕，已移除 ThreadLocal 引用。");
                    }
                }
            }
        }
    }
}