package com.myflow.runtime;

import com.myflow.definition.model.Node;
import com.myflow.definition.model.NodeType;
import com.myflow.runtime.behavior.NodeBehavior;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class BehaviorFactory {

    @SneakyThrows
    public static NodeBehavior createBehavior(NodeType type, Node childNode) {
        Class<? extends NodeBehavior> behaviorClass = type.getBehaviorClass();
        Constructor<?> constructor = behaviorClass.getConstructor(getParameterTypes(childNode));
        NodeBehavior behavior = (NodeBehavior) constructor.newInstance(childNode);
        return behavior;
    }

    private static Class<?>[] getParameterTypes(Object... args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return parameterTypes;
    }
}
