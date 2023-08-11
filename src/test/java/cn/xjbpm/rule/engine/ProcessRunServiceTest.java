package cn.xjbpm.rule.engine;

import cn.hutool.core.io.IoUtil;
import cn.xjbpm.rule.RuleEngineApplication;
import cn.xjbpm.rule.engine.common.utils.JsonUtil;
import cn.xjbpm.rule.engine.runtime.ProcessRunService;
import cn.xjbpm.rule.engine.runtime.context.ProcessRuntimeContext;
import cn.xjbpm.rule.engine.runtime.entity.ProcessInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RuleEngineApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProcessRunServiceTest {

    @Autowired
    ProcessRunService processRunService;

    @Test
    public void startTestGrsdsjs() {
        String requestJson = IoUtil.readUtf8(ProcessRunServiceTest.class.getResourceAsStream("/process/个人所得税计算_request.json"));
        Map map = JsonUtil.json2Obj(requestJson, Map.class);
        StopWatch sw = new StopWatch();
        for (int i = 0; i < 1; i++) {
            sw.start("task_" + i);
            try {
                ProcessRuntimeContext processRuntimeContext = ProcessRuntimeContext.builder()
                        .key("grsds")
                        .environment("dev")
                        .variable(map)
                        .build();
                ProcessInstance result = processRunService.start(processRuntimeContext);
                System.out.println("返回：" + JsonUtil.obj2Json(result));
            } finally {
                sw.stop();
                log.info("耗时:{}ms", sw.getLastTaskTimeMillis());
            }
        }
        log.info("总耗时:{}ms", sw.getTotalTimeMillis());
    }


}
