package com.myflow.definition.validator;

import com.myflow.definition.model.Node;
import com.myflow.definition.model.NodeType;
import com.myflow.definition.model.ProcessModel;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class ProcessModelValidator {


    /**
     * 校验
     *
     * @param processModel
     * @return
     */
    public List<ErrorNodeMsg> check(ProcessModel processModel) {
        List<? extends Node> childNodes = processModel.getChildNodes();
        List<ErrorNodeMsg> errorNodeMsgs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Node childNode : childNodes) {
                NodeType type = childNode.getType();
                Class<? extends NodeValidator> validatorClass = type.getValidatorClass();
                try {
                    //TODO 性能待优化
                    validatorClass.newInstance().check(childNode);
                } catch (Exception e) {
                    errorNodeMsgs.add(new ErrorNodeMsg(childNode.getKey(), type, e.getMessage()));
                }
            }
        }
        return errorNodeMsgs;
    }
}
