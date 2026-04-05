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

package cn.xjbpm.rule.engine;

import cn.hutool.core.io.IoUtil;
import cn.xjbpm.rule.RuleEngineApplication;
import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcuteRuleFlow;
import cn.xjbpm.rule.dto.ExcuteRuleFlowResult;
import cn.xjbpm.rule.engine.runtime.RuleFlowExcuteService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.io.InputStream;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@SuppressWarnings("all")
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RuleEngineApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProcessRunServiceTest {

    @Autowired
    RuleFlowExcuteService processRunService;

    @Test
    public void test() {
        startTestGrsdsjs();
        System.out.println("-----------");
        startTestGrsdsjs();
    }

    @Test
    public void startTestGrsdsjs() {
        InputStream resourceAsStream = ProcessRunServiceTest.class.getResourceAsStream("/process/个人所得税计算.json");
        String processDefinitionContent = IoUtil.readUtf8(resourceAsStream);
        String requestJson = IoUtil.readUtf8(ProcessRunServiceTest.class.getResourceAsStream("/process/个人所得税计算_request.json"));
        Map map = JsonUtils.json2Obj(requestJson, Map.class);
        StopWatch sw = new StopWatch();
        for (int i = 0; i < 1; i++) {
            sw.start("task_" + i);
            try {
                ExcuteRuleFlow createProcessRequest = new ExcuteRuleFlow();
                createProcessRequest.setKey("grsds");
                createProcessRequest.setVariables(map);
                createProcessRequest.setContent(processDefinitionContent);
                ExcuteRuleFlowResult result = processRunService.startFlow(createProcessRequest);
                System.out.println("返回：" + JsonUtils.obj2Json(result));
            } finally {
                sw.stop();
                log.info("耗时:{}ms", sw.getLastTaskTimeMillis());
            }
        }
        log.info("总耗时:{}ms", sw.getTotalTimeMillis());
    }


}