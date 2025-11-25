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

import cn.xjbpm.rule.custom.ProcessDefinitionService;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.repository.entity.RuleFlowEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 */
@Service
@AllArgsConstructor
public class DefaultProcessDefinitionService implements ProcessDefinitionService {

    private final RuleFlowService ruleFlowService;

    @Override
    public ProcessModel getProcessModel(String key, Integer version) {
        RuleFlowEntity entity;
        if (Objects.nonNull(version)) {
            entity = ruleFlowService.findByKeyAndVerison(key, version);
        } else {
            entity = ruleFlowService.findByKeyAndMaxVerison(key);
        }
        ProcessModel processModel = convertToModel(entity.getContent());
        return processModel;
    }

    @Override
    public ProcessModel convertToModel(String content) {
        return ProcessDefinitionService.super.convertToModel(content);
    }
}