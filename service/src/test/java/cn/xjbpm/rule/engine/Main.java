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
package cn.xjbpm.rule.engine;

import akka.actor.ActorSystem;
import cn.hutool.core.io.IoUtil;
import cn.xjbpm.rule.common.constant.ProcessConstant;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.VariableTranslateUtils;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
import cn.xjbpm.rule.engine.definition.parse.ProcessModelParse;
import cn.xjbpm.rule.engine.runtime.actor.AkkaRuleFlowScheduler;
import cn.xjbpm.rule.engine.runtime.actor.NodeDependencyBuilder;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/20
 */
@Slf4j
public class Main {
    private static final ProcessModelParse PROCESS_MODEL_JSON_CONVERTER = new ProcessModelParse();

    public static void main(String[] args) {
        InputStream resourceAsStream = Main.class.getResourceAsStream("/process/个人所得税计算.json");
        String processDefinitionContent = IoUtil.readUtf8(resourceAsStream);
        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(processDefinitionContent);
        NodeDependencyBuilder nodeDependencyBuilder = new NodeDependencyBuilder();
        nodeDependencyBuilder.buildNodeDependency(processModel.getChildNodes());
        String requestJson = IoUtil.readUtf8(ProcessRunServiceTest.class.getResourceAsStream("/process/个人所得税计算_request.json"));
        ActorSystem actorSystem = ActorSystem.create("FlowSystem");
        AkkaRuleFlowScheduler scheduler = new AkkaRuleFlowScheduler(actorSystem);
        try {
            for (int i = 0; i < 3; i++) {
                Map<String, Object> runtimeVar = new HashMap<>();
                runtimeVar.put(ProcessConstant.BUSINESS_OBJECTS, VariableTranslateUtils.translate(processModel.getBusinessObjectModels(),
                        false, JsonUtils.json2Obj(requestJson, Map.class)));
                long startTime = System.currentTimeMillis();
                FlowContext flowContext = new FlowContext(runtimeVar);
                scheduler.startFlow(processModel, flowContext);
                log.info("总耗时：{}ms", (System.currentTimeMillis() - startTime));
                log.info("流程执行完成！");
            }
        } catch (Exception e) {
            log.error("流程执行出错：{}", e.getMessage(), e);
        } finally {
            // 4. 关闭 Akka 系统
            actorSystem.terminate();
        }
    }
}