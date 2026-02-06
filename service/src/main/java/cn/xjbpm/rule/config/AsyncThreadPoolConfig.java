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

package cn.xjbpm.rule.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@EnableAsync
@Slf4j
public class AsyncThreadPoolConfig implements AsyncConfigurer {

    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static final int CORE_POOL_SIZE = PROCESSORS;

    private static final int MAXIMUM_POOL_SIZE = CORE_POOL_SIZE * 2;

    private static final int QUEUE_CAPACITY = 100;

    private static final int KEEP_ALIVE_SECONDS = 60;

    private static final int AWAIT_TERMINATION_SECONDS = 60;

    private static final RejectedExecutionHandler REJECTED_EXECUTION_HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAXIMUM_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix("async-thread-pool-executor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        executor.setRejectedExecutionHandler(REJECTED_EXECUTION_HANDLER);
        executor.initialize();
        return executor;
    }

}