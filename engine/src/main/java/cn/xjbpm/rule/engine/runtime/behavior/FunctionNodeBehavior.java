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
package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.FunctionNode;
import cn.xjbpm.rule.engine.runtime.behavior.model.FileModel;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class FunctionNodeBehavior implements NodeBehavior {

    private final FunctionNode node;

    public FunctionNodeBehavior(FunctionNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) {
        log.info("执行函数");
        if (Objects.equals(node.getScriptType(), "aviator")) {
            AviatorContext aviatorContext = AviatorContext.builder()
                    .cached(true)
                    .expression(node.getScriptContent())
                    .env(context.getVariable())
                    .build();
            Map<String, Object> resultMap = new HashMap<>();
            Object result = AviatorExecutor.execute(aviatorContext);
            resultMap.put("result", result);
            context.put(node.getId(), resultMap);
        } else {
            log.info("未实现脚本类型:{}", node.getScriptType());
        }
    }
}