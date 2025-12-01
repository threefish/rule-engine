
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
package cn.xjbpm.rule.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/1
 */
@Data
public class RuleFlowTestExcuteVO {

    /**
     * 需要跳过的节点
     */
    private Set<String> skipNodeIds;
    /**
     * 流程定义key
     */
    @NotBlank
    private String key;
    /**
     * 流程变量
     */
    @NotNull
    private Map<String, Object> variables;
    /**
     * 流程定义内容
     */
    @NotBlank
    private String content;

}
