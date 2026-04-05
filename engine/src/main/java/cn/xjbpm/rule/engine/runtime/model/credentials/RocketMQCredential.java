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
package cn.xjbpm.rule.engine.runtime.model.credentials;

import lombok.Data;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * RocketMQ凭据
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class RocketMQCredential {

    /**
     * NameServer地址
     * 多个用分号分隔，如：192.168.1.1:9876;192.168.1.2:9876
     */
    private String nameServer;

    /**
     * 生产者组名
     */
    private String groupName = "DEFAULT_PRODUCER_GROUP";

    /**
     * Access Key（ACL认证时使用）
     */
    private String accessKey;

    /**
     * Secret Key（ACL认证时使用）
     */
    private String secretKey;

    /**
     * 发送超时时间（毫秒）
     */
    private int sendTimeout = 3000;

    /**
     * 消息压缩阈值（字节）
     */
    private int compressThreshold = 4096;

    /**
     * 重试次数
     */
    private int retryTimes = 2;

    /**
     * 缓存的Producer实例（由RocketMQClientManager管理）
     */
    private DefaultMQProducer producer;

}
