package com.myflow.runtime.behavior;

import com.myflow.definition.model.gateway.ParallelGatewayNode;
import com.myflow.runtime.entity.ExecutionEntity;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class ParallelGatewayNodeBehavior implements NodeBehavior {

    private final ParallelGatewayNode node;

    public ParallelGatewayNodeBehavior(ParallelGatewayNode node) {
        this.node = node;
    }

    @Override
    public void execution(ExecutionEntity executionEntity) {

    }

    @Override
    public void leave(ExecutionEntity executionEntity) {

    }
}
