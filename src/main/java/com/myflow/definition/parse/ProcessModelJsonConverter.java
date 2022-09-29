package com.myflow.definition.parse;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.myflow.definition.model.Node;
import com.myflow.definition.model.NodeType;
import com.myflow.definition.model.ProcessModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 */
public class ProcessModelJsonConverter {


    /**
     * 转换成模型
     *
     * @param json
     * @return
     */
    public ProcessModel convertToModel(String json) {
        JSON parse = JSONUtil.parse(json);
        ProcessModel processModel = parse.toBean(ProcessModel.class);
        JSONArray jsonArray = ((JSONObject) parse).getJSONArray("childNodes");
        List<? extends Node> childNodes = new ArrayList<>();

        for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            NodeType type = jsonObject.get("type", NodeType.class);
            childNodes.add(jsonObject.toBean((Type) type.getClazz()));
        }
        processModel.setChildNodes(childNodes);

        return processModel;
    }

    /**
     * 转换成json
     *
     * @param processModel
     * @return
     */
    public String convertToJson(ProcessModel processModel) {
        return JSONUtil.toJsonStr(processModel);
    }
}
