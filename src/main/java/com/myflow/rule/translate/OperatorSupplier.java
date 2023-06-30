package com.myflow.rule.translate;

import com.myflow.rule.Rule;

@FunctionalInterface
public interface OperatorSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    String get(Rule rule);
}