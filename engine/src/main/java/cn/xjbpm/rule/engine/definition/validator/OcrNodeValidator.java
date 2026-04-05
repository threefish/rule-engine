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

import cn.xjbpm.rule.engine.definition.model.nodes.OcrNode;
import cn.xjbpm.rule.engine.definition.model.nodes.OcrNode.ImageSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * OCR节点验证器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class OcrNodeValidator extends BaseNodeValidator<OcrNode> {

    @Override
    public void check(OcrNode node) {
        super.check(node);

        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()),
                "OCR凭据不能为空");

        Assert.notNull(node.getOcrType(),
                "识别类型不能为空");

        Assert.notNull(node.getImageSource(),
                "图像输入源不能为空");

        switch (node.getImageSource()) {
            case BASE64:
                Assert.isTrue(StringUtils.isNotBlank(node.getImageData()),
                        "Base64图像数据不能为空");
                break;
            case URL:
                Assert.isTrue(StringUtils.isNotBlank(node.getImageUrl()),
                        "图像URL不能为空");
                break;
            case VARIABLE:
                Assert.isTrue(StringUtils.isNotBlank(node.getImageData()),
                        "图像变量路径不能为空");
                break;
            default:
                break;
        }
    }
}
