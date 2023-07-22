package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.NodeType;
import cn.xjbpm.rule.engine.definition.model.ProcessModel;
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
