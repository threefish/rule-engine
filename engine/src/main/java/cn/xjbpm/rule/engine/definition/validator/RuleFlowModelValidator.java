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
package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class RuleFlowModelValidator {

    /**
     * 内置校验器
     */
    public static final Map<NodeType, NodeValidator> nodeValidatorMap = new ConcurrentHashMap<>();
    /**
     * 自定义校验器
     */
    public static final Map<NodeType, NodeValidator> customNodeValidatorMap = new ConcurrentHashMap<>();

    /**
     * 允许外部程序注入自定义校验类或对象。
     * * @param type 节点类型
     *
     * @param validator 校验器实例
     */
    public static void registerValidator(NodeType type, NodeValidator validator) {
        if (type != null && validator != null) {
            customNodeValidatorMap.put(type, validator);
        }
    }

    /**
     * 校验方法
     *
     * @param ruleFlowModel 规则流程模型
     * @return 错误节点信息列表
     */
    public List<ErrorNodeMsg> check(RuleFlowModel ruleFlowModel) {
        List<? extends Node> childNodes = ruleFlowModel.getChildNodes();
        List<ErrorNodeMsg> errorNodeMsgs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Node childNode : childNodes) {
                NodeType type = childNode.getType();
                NodeValidator validator = nodeValidatorMap.computeIfAbsent(type, k -> {
                    try {
                        return type.getValidatorClass().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("无法创建校验器实例: " + k, e);
                    }
                });
                try {
                    validator.check(childNode);
                } catch (Exception e) {
                    errorNodeMsgs.add(new ErrorNodeMsg(childNode.getId(), type, e.getMessage()));
                }
                NodeValidator nodeValidator = customNodeValidatorMap.get(type);
                if (Objects.nonNull(nodeValidator)) {
                    try {
                        nodeValidator.check(childNode);
                    } catch (Exception e) {
                        errorNodeMsgs.add(new ErrorNodeMsg(childNode.getId(), type, e.getMessage()));
                    }
                }
            }
        }
        return errorNodeMsgs;
    }
}