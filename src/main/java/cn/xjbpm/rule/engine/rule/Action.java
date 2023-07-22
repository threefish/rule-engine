package cn.xjbpm.rule.engine.rule;

import cn.xjbpm.rule.engine.rule.enums.ActionType;
import cn.xjbpm.rule.engine.rule.enums.AssignmentType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/29
 */
@Data
@Builder
public class Action {
    private String key;
    private ActionType type;
    private String left;
    private String fieldValue;
    private String expressionValue;
    private List<Object> values;
    private AssignmentType assignmentType;
}
