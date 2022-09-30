package com.myflow;

import com.myflow.definition.model.EndNode;
import com.myflow.definition.model.ProcessModel;
import com.myflow.definition.model.SequenceConnNode;
import com.myflow.definition.model.StartNode;
import com.myflow.definition.model.activity.FunctionActivityNode;
import com.myflow.definition.parse.ProcessModelParse;
import com.myflow.definition.validator.ErrorNodeMsg;
import com.myflow.definition.validator.ProcessModelValidator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
public class ProcessModelJsonConverterTest {

    private static final ProcessModelParse PROCESS_MODEL_JSON_CONVERTER = new ProcessModelParse();

    @Test
    void convertToJson() {

        ProcessModel processModel = new ProcessModel();
        processModel.setKey("test");
        processModel.setName("测试流程");
        processModel.setDocumentation("测试流程");

        StartNode startNode = new StartNode();
        startNode.setKey("start");
        startNode.setName("流程开始");


        FunctionActivityNode fun1 = new FunctionActivityNode();
        fun1.setKey("f1");
        fun1.setName("函数1节点");
        fun1.setCode("FUN001");


        EndNode endNode = new EndNode();
        endNode.setKey("end");
        endNode.setName("流程结束");


        SequenceConnNode start_to_f1 = new SequenceConnNode();
        start_to_f1.setKey("start->>f1");
        start_to_f1.setSourceNodeKey("start");
        start_to_f1.setTargetNodeKey("f1");


        SequenceConnNode f1_to_end = new SequenceConnNode();
        f1_to_end.setKey("f1->>end");
        f1_to_end.setSourceNodeKey("f1");
        f1_to_end.setTargetNodeKey("end");

        processModel.setChildNodes(Arrays.asList(startNode, endNode, fun1, f1_to_end, f1_to_end));

        String json = PROCESS_MODEL_JSON_CONVERTER.convertToJson(processModel);

        System.out.println(json);
    }


    @Test
    void convertToModel() {
        String json = "{\n" +
                "    \"key\":\"test\",\n" +
                "    \"name\":\"测试\",\n" +
                "    \"documentation\":\"一个测试流程\",\n" +
                "    \"executionListeners\":[],\n" +
                "    \"nodes\": [\n" +
                "        {\n" +
                "            \"id\": \"start-00001\",\n" +
                "            \"type\": \"StartNode\",\n" +
                "            \"x\": 320,\n" +
                "            \"y\": 140,\n" +
                "            \"properties\": {\n" +
                "                \"name\": \"开始\",\n" +
                "                \"tag\": \"A1\"\n" +
                "            },\n" +
                "            \"text\": {\n" +
                "                \"x\": 320,\n" +
                "                \"y\": 140,\n" +
                "                \"value\": \"开始\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"end-00001\",\n" +
                "            \"type\": \"EndNode\",\n" +
                "            \"x\": 640,\n" +
                "            \"y\": 160,\n" +
                "            \"properties\": {\n" +
                "                \"name\": \"结束\",\n" +
                "                \"tag\": \"A2\"\n" +
                "            },\n" +
                "            \"text\": {\n" +
                "                \"x\": 640,\n" +
                "                \"y\": 160,\n" +
                "                \"value\": \"结束\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"00001\",\n" +
                "            \"type\": \"FunctionActivityNode\",\n" +
                "            \"x\": 460,\n" +
                "            \"y\": 260,\n" +
                "            \"properties\": {\n" +
                "                \"name\": \"执行函数\",\n" +
                "                \"tag\": \"A3\"\n" +
                "            },\n" +
                "            \"text\": {\n" +
                "                \"x\": 460,\n" +
                "                \"y\": 260,\n" +
                "                \"value\": \"执行函数\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"edges\": [\n" +
                "        {\n" +
                "            \"id\": \"e435db55-6279-4b4a-9b37-a724589ccc65\",\n" +
                "            \"type\": \"SequenceConnNode\",\n" +
                "            \"sourceNodeId\": \"start-00001\",\n" +
                "            \"targetNodeId\": \"00001\",\n" +
                "            \"startPoint\": {\n" +
                "                \"x\": 342,\n" +
                "                \"y\": 140\n" +
                "            },\n" +
                "            \"endPoint\": {\n" +
                "                \"x\": 460,\n" +
                "                \"y\": 220\n" +
                "            },\n" +
                "            \"properties\": {},\n" +
                "            \"pointsList\": [\n" +
                "                {\n" +
                "                    \"x\": 342,\n" +
                "                    \"y\": 140\n" +
                "                },\n" +
                "                {\n" +
                "                    \"x\": 442,\n" +
                "                    \"y\": 140\n" +
                "                },\n" +
                "                {\n" +
                "                    \"x\": 460,\n" +
                "                    \"y\": 120\n" +
                "                },\n" +
                "                {\n" +
                "                    \"x\": 460,\n" +
                "                    \"y\": 220\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"5cf962a1-001d-45e4-b93f-853accb815b7\",\n" +
                "            \"type\": \"SequenceConnNode\",\n" +
                "            \"sourceNodeId\": \"00001\",\n" +
                "            \"targetNodeId\": \"end-00001\",\n" +
                "            \"startPoint\": {\n" +
                "                \"x\": 460,\n" +
                "                \"y\": 300\n" +
                "            },\n" +
                "            \"endPoint\": {\n" +
                "                \"x\": 640,\n" +
                "                \"y\": 182\n" +
                "            },\n" +
                "            \"properties\": {},\n" +
                "            \"pointsList\": [\n" +
                "                {\n" +
                "                    \"x\": 460,\n" +
                "                    \"y\": 300\n" +
                "                },\n" +
                "                {\n" +
                "                    \"x\": 460,\n" +
                "                    \"y\": 400\n" +
                "                },\n" +
                "                {\n" +
                "                    \"x\": 640,\n" +
                "                    \"y\": 282\n" +
                "                },\n" +
                "                {\n" +
                "                    \"x\": 640,\n" +
                "                    \"y\": 182\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(json);

        ProcessModelValidator processModelValidator = new ProcessModelValidator();
        List<ErrorNodeMsg> errorNodeMsgs = processModelValidator.check(processModel);

        String json2 = PROCESS_MODEL_JSON_CONVERTER.convertToJson(processModel);

        System.out.println(processModel);
        System.out.println(errorNodeMsgs);
        System.out.println(json2);
    }

}
