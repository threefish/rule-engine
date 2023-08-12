package cn.xjbpm.rule.engine.runtime.exception;

import cn.xjbpm.rule.engine.rule.enums.ActionType;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/1
 */
public class ActionExcuteException extends RuntimeException {


    private final ActionType actionType;

    public ActionExcuteException(ActionType actionType) {
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }
}
