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
package cn.xjbpm.rule.service;

import cn.xjbpm.rule.custom.RuleFlowDefinitionService;
import cn.xjbpm.rule.engine.definition.model.RuleFlowModel;
import cn.xjbpm.rule.vo.RuleFlowVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 */
@Service
@AllArgsConstructor
public class DefaultProcessDefinitionService implements RuleFlowDefinitionService {

    private final RuleFlowService ruleFlowService;

    @Override
    public RuleFlowModel getModel(String key) {
        RuleFlowVO entity = ruleFlowService.findByKeyAndDeployed(key);
        RuleFlowModel processModel = convertToModel(entity.getContent());
        processModel.setKey(entity.getKey());
        processModel.setName(entity.getName());
        processModel.setDescription(entity.getDescription());
        return processModel;
    }

    @Override
    public RuleFlowModel convertToModel(String content) {
        return RuleFlowDefinitionService.super.convertToModel(content);
    }
}