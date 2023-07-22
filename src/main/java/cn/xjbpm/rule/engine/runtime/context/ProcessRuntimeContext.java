package cn.xjbpm.rule.engine.runtime.context;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Data
@Builder
public class ProcessRuntimeContext {

    /**
     * 流程定义key
     */
    private String key;
    /**
     * 版本号，null取最新
     */
    private Integer version;
    /**
     * 可重入,会持久化
     */
    private boolean reentrant;
    /**
     * 流程变量
     */
    private Map<String, Object> variable;

}
