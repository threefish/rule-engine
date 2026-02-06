/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.xjbpm.rule.engine.rule.translate;

import cn.xjbpm.rule.engine.rule.enums.OperatorType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/6/30
 */
public class ListTranslate extends AbstractTranslate {


    private static final Map<OperatorType, OperatorSupplier> OPERATOR_TYPE_CACHE = new HashMap() {
        {

            put(OperatorType.IS_EMPTY, (OperatorSupplier) rule -> String.format("LIST_IS_EMPTY(%s)", rule.getName()));
            put(OperatorType.IS_NOT_EMPTY, (OperatorSupplier) rule -> String.format("!LIST_IS_EMPTY(%s)", rule.getName()));

            put(OperatorType.CONTAINS, (OperatorSupplier) rule -> String.format("CONTAINS(%s,%s)", rule.getName(), rule.getValue()));
            put(OperatorType.NOT_CONTAINS, (OperatorSupplier) rule -> String.format("!CONTAINS(%s,%s)", rule.getName(), rule.getValue()));

            put(OperatorType.IS_NULL, (OperatorSupplier) rule -> String.format("IS_NULL(%s)", rule.getName()));
            put(OperatorType.IS_NOT_NULL, (OperatorSupplier) rule -> String.format("IS_NOT_NULL(%s)", rule.getName()));
        }
    };


    @Override
    protected OperatorSupplier getOperatorSupplier(OperatorType operator) {
        return OPERATOR_TYPE_CACHE.get(operator);
    }

}