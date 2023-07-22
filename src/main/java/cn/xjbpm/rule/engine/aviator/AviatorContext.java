package cn.xjbpm.rule.engine.aviator;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
@Builder
@Data
public class AviatorContext implements Serializable {
    /**
     * 表达式
     */
    private String expression;
    /**
     * 表达式参数
     */
    private Map<String, Object> env;
    /**
     * 是否缓存
     */
    private boolean cached;


    public static AviatorContext create(String expression, Map<String, Object> env) {
        return AviatorContext.builder().expression(expression).cached(true).env(env).build();
    }
}
