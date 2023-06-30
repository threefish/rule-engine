package com.myflow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myflow.common.utils.VariableTranslateUtils;
import com.myflow.definition.model.ObjectModel;
import com.myflow.definition.model.ProcessModel;
import com.myflow.definition.parse.ProcessModelParse;
import com.myflow.rule.enums.VariableType;
import com.myflow.runtime.DefaultProcessRunService;
import com.myflow.runtime.ProcessRunService;
import com.myflow.runtime.context.ProcessRuntimeContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class ProcessRunServiceTest {
    private static final ProcessModelParse PROCESS_MODEL_JSON_CONVERTER = new ProcessModelParse();

    @Test
    void startTest() {
        InputStream resourceAsStream = ProcessRunServiceTest.class.getResourceAsStream("/process/test.json");
        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(IoUtil.readUtf8(resourceAsStream));

        Map<String, Object> variable = new HashMap<>();
        variable.put("age", 18);

        ProcessRuntimeContext processRuntimeContext = new ProcessRuntimeContext(processModel, variable);

        ProcessRunService processRunService = new DefaultProcessRunService();

        Long processId = processRunService.start(processRuntimeContext);

        System.out.println("流程ID：" + processId);
    }


    @Test
    void startTestGrsdsjs() {
        InputStream resourceAsStream = ProcessRunServiceTest.class.getResourceAsStream("/process/个人所得税计算.json");
        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(IoUtil.readUtf8(resourceAsStream));

        String requestJson = IoUtil.readUtf8(ProcessRunServiceTest.class.getResourceAsStream("/process/个人所得税计算_request.json"));

        Map<String, Object> variable = new HashMap<>();
        variable.put("业务对象",VariableTranslateUtils.translate(processModel.getBusinessObjectModels(), requestJson));

        ProcessRuntimeContext processRuntimeContext = new ProcessRuntimeContext(processModel, variable);

        ProcessRunService processRunService = new DefaultProcessRunService();

        Long processId = processRunService.start(processRuntimeContext);

        System.out.println("流程ID：" + processId);
    }


}
