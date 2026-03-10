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

package cn.xjbpm.rule.node;

import cn.xjbpm.rule.node.enums.TriggerMode;
import cn.xjbpm.rule.node.model.TriggerRule;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/9
 */
@Data
public class StartNodeProperties {

    /**
     * 触发方式
     */
    private TriggerMode triggerMode;

    /**
     * 请求参数--TriggerMode.SCHEDULED
     */
    private String requestParams;
    /**
     * 触发规则列表--TriggerMode.SCHEDULED
     */
    private List<TriggerRule> triggers;


    /**
     * 链接凭据 --TriggerMode.RABBIT_MQ
     */
    private String rabbitMQCredentialId;
    /**
     * 队列名称 --TriggerMode.RABBIT_MQ
     */
    private String rabbitMQQueueName;
    /**
     * 自动确认消息 --TriggerMode.RABBIT_MQ
     */
    private boolean rabbitMQAutoAck;
    /**
     * 消费者预取消息数量 --TriggerMode.RABBIT_MQ
     */
    private int rabbitMQPrefetchCount;

    /**
     * 链接凭据--TriggerMode.KAFKA
     */
    private String kafkaCredentialId;
    /**
     * Topic--TriggerMode.KAFKA
     */
    private String kafkaTopic;
    /**
     * 消费者组--TriggerMode.KAFKA
     */
    private String kafkaGroupId;
    /**
     * 偏移重置策略 --TriggerMode.KAFKA
     * {label: '最早', value: 'EARLIEST'}, {label: '最新', value: 'LATEST'}, {label: '无', value: 'NONE'},
     */
    private String kafkaOffsetReset;
    /**
     * 自动提交--TriggerMode.KAFKA
     */
    private boolean kafkaAutoCommit;
    /**
     * 消费等待超时时间(秒)--TriggerMode.KAFKA
     */
    private int kafkaTimeout;
    /**
     * 单次接收的最大消息数--TriggerMode.KAFKA
     */
    private int kafkaMaxPollRecords;


    /**
     * 链接凭据--TriggerMode.MQTT
     */
    private String mqttCredentialId;

    /**
     * Topic--TriggerMode.MQTT
     */
    private String mqttTopic;
    /**
     * QoS级别--TriggerMode.MQTT
     * {label: '0 - 最多一次', value: 0}, {label: '1 - 至少一次', value: 1}, {label: '2 - 恰好一次', value: 2},
     */
    private int mqttQos;
    /**
     * 断开连接时清除订阅状态--TriggerMode.MQTT
     */
    private boolean mqttCleanSession;

    /**
     * 客户端ID(不填写则自动生成)--TriggerMode.MQTT
     */
    private String mqttClientId;

    /**
     * 订阅等待超时时间(秒)--TriggerMode.MQTT
     */
    private boolean mqttTimeout;

    /**
     * 单次接收的最大消息数--TriggerMode.MQTT
     */
    private int mqttMaxMessages;


}