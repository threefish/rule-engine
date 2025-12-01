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
package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.engine.definition.model.DelayWaitNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class DelayWaitNodeValidator extends BaseNodeValidator<DelayWaitNode> {

    @Override
    public void check(DelayWaitNode node) {
        super.check(node);
        Assert.notNull(node.getDelayTime(), "延时时间不能为空");
        Assert.notNull(node.getDelayTime() < 0, "延时时间不能小于0");
    }
}