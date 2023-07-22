package cn.xjbpm.rule.engine.rule.translate;

import cn.xjbpm.rule.engine.rule.Rule;

@FunctionalInterface
public interface OperatorSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    String get(Rule rule);
}