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

package cn.xjbpm.rule.custom;

import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.engine.definition.parse.RuleFlowModelParse;
import cn.xjbpm.rule.engine.definition.validator.ErrorNodeMsg;
import cn.xjbpm.rule.engine.definition.validator.RuleFlowModelValidator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 */
public interface RuleFlowModelCacheManager {

    RuleFlowModelParse RULE_FLOW_MODEL_PARSE = new RuleFlowModelParse();
    RuleFlowModelValidator RULE_FLOW_MODEL_VALIDATOR = new RuleFlowModelValidator();


    RuleFlowModel getModel(String key);


    default RuleFlowModel convertToModel(String content) {
        RuleFlowModel ruleFlowMode = RULE_FLOW_MODEL_PARSE.convertToModel(content);
        List<ErrorNodeMsg> errorNodeMsgs = RULE_FLOW_MODEL_VALIDATOR.check(ruleFlowMode);
        if (!CollectionUtils.isEmpty(errorNodeMsgs)) {
            List<String> errors = new ArrayList<>();
            for (ErrorNodeMsg errorNodeMsg : errorNodeMsgs) {
                errors.add(String.format("节点:%s 类型:%s 原因:%s", errorNodeMsg.getKey(), errorNodeMsg.getType(), errorNodeMsg.getMessage()));
            }
            throw new RuntimeException(String.join("\n", errors));
        }
        Assert.notNull(ruleFlowMode.getStartNode(), "开始节点不可为空！");
        return ruleFlowMode;
    }
}