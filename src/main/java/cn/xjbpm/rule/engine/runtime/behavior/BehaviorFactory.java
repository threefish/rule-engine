package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.definition.model.Node;
import cn.xjbpm.rule.engine.definition.model.NodeType;
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
