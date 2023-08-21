package cn.xjbpm.rule.engine.runtime.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/8/20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionScope {
    /**
     * 上级节点key
     */
    String parentNodeKey;
    /**
     * 上级节点执行ID
     */
    Long parentNodeExecutionId;
    /**
     * 是清空的
     */
    boolean cleared;
}
