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
package cn.xjbpm.rule.custom;

import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.definition.parse.ProcessModelParse;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/22
 */
public interface ProcessDefinitionService {

    ProcessModelParse PROCESS_MODEL_JSON_CONVERTER = new ProcessModelParse();


    ProcessModel getProcessModel(String key, Integer version);


    default ProcessModel convertToModel(String content) {
        return PROCESS_MODEL_JSON_CONVERTER.convertToModel(content);
    }
}