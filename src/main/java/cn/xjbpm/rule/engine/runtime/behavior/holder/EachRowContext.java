package cn.xjbpm.rule.engine.runtime.behavior.holder;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/1
 */
public class EachRowContext {
    /**
     * 等待删除
     */
    @Getter
    private final List<Object> waitDeleteRows = new ArrayList<>();

    public void addDeleteObject(Object object) {
        waitDeleteRows.add(object);
    }
}
