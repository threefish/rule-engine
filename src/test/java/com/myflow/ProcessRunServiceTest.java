package com.myflow;

import cn.hutool.core.io.IoUtil;
import com.myflow.definition.model.ProcessModel;
import com.myflow.definition.parse.ProcessModelParse;
import com.myflow.runtime.DefaualtProcessRunService;
import com.myflow.runtime.ProcessRunService;
import com.myflow.runtime.context.ProcessRuntimeContext;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class ProcessRunServiceTest {
    private static final ProcessModelParse PROCESS_MODEL_JSON_CONVERTER = new ProcessModelParse();

    @Test
    void start() {
        InputStream resourceAsStream = ProcessRunServiceTest.class.getResourceAsStream("/process/test.json");
        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(IoUtil.readUtf8(resourceAsStream));

        Map<String, Object> variable = new HashMap<>();
        variable.put("age", 18);

        ProcessRuntimeContext processRuntimeContext = new ProcessRuntimeContext(processModel, variable);

        ProcessRunService processRunService = new DefaualtProcessRunService();

        Long processId = processRunService.start(processRuntimeContext);

        System.out.println("流程ID：" + processId);
    }
}
