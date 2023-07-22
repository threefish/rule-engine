package cn.xjbpm.rule.engine.definition.model.event;

import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/29
 * 定义
 */
@Data
public class ExcutionListener {

    String event;

    String executeExpression;

}
