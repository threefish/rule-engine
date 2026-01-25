/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.engine.definition.model.nodes.Node;
import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
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