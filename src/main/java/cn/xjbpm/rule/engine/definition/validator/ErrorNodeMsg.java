package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.engine.definition.model.NodeType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Data
@AllArgsConstructor
public class ErrorNodeMsg {
    String key;
    NodeType type;
    String message;
}
