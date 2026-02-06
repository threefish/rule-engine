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

package cn.xjbpm.rule.engine.definition.validator;

import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.AITTSNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class AITTSNodeValidator extends BaseNodeValidator<AITTSNode> {

    @Override
    public void check(AITTSNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), "链接凭据不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getPrompt()), "文本内容不能为空");
        if (node.getSpeaker() == AITTSNode.Speaker.multiSpeaker) {
            Assert.notEmpty(node.getMultiSpeakerVoiceConfig(), "多音色配置不能为空");
            for (AITTSNode.MultiSpeaker multiSpeaker : node.getMultiSpeakerVoiceConfig()) {
                Assert.isTrue(StringUtils.isNotBlank(multiSpeaker.getVoice()), "多音色配置的音色不能为空");
                Assert.isTrue(StringUtils.isNotBlank(multiSpeaker.getSpeaker()), "说话者不能为空");
            }
        } else {
            Assert.isTrue(StringUtils.isNotBlank(node.getVoice()), "音色不能为空");
        }
    }
}