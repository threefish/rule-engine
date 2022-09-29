package com.myflow;

import com.myflow.definition.model.EndNode;
import com.myflow.definition.model.ProcessModel;
import com.myflow.definition.model.SequenceConnNode;
import com.myflow.definition.model.StartNode;
import com.myflow.definition.model.activity.FunctionActivityNode;
import com.myflow.definition.parse.ProcessModelJsonConverter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
public class ProcessModelJsonConverterTest {

    private static final ProcessModelJsonConverter PROCESS_MODEL_JSON_CONVERTER = new ProcessModelJsonConverter();

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

        processModel.setChildNodes(Arrays.asList(startNode, endNode,fun1, f1_to_end, f1_to_end));

        String json = PROCESS_MODEL_JSON_CONVERTER.convertToJson(processModel);

        System.out.println(json);
    }


    @Test
    void convertToModel() {
        String json = "{\"childNodes\":[{\"outgoingNodes\":[],\"incomingNodes\":[],\"type\":\"StartNode\",\"executionListeners\":[],\"name\":\"流程开始\",\"key\":\"start\",\"order\":0},{\"outgoingNodes\":[],\"incomingNodes\":[],\"type\":\"EndNode\",\"executionListeners\":[],\"name\":\"流程结束\",\"key\":\"end\",\"order\":0},{\"ignoreFailure\":false,\"outgoingNodes\":[],\"incomingNodes\":[],\"code\":\"FUN001\",\"type\":\"FunctionActivityNode\",\"retryTimes\":0,\"executionListeners\":[],\"asynchronous\":false,\"name\":\"函数1节点\",\"retry\":false,\"key\":\"f1\",\"order\":0},{\"outgoingNodes\":[],\"incomingNodes\":[],\"sourceNodeKey\":\"f1\",\"type\":\"SequenceConnNode\",\"executionListeners\":[],\"targetNodeKey\":\"end\",\"key\":\"f1->>end\",\"order\":0},{\"outgoingNodes\":[],\"incomingNodes\":[],\"sourceNodeKey\":\"f1\",\"type\":\"SequenceConnNode\",\"executionListeners\":[],\"targetNodeKey\":\"end\",\"key\":\"f1->>end\",\"order\":0}],\"documentation\":\"测试流程\",\"executionListeners\":[],\"name\":\"测试流程\",\"key\":\"test\"}";

        ProcessModel processModel = PROCESS_MODEL_JSON_CONVERTER.convertToModel(json);

        System.out.println(processModel);
    }

}
