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
import cn.xjbpm.rule.engine.definition.model.nodes.WordNode;
import org.springframework.util.Assert;

/**
 * Word文档生成节点验证器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class WordNodeValidator extends BaseNodeValidator<WordNode> {

    @Override
    public void check(WordNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getTemplatePath()), "模板文件路径不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getOutputPath()), "输出文件路径不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getDataVariable()), "数据源变量名不能为空");
        Assert.isTrue(isWordFile(node.getTemplatePath()), "模板文件格式不支持，仅支持.docx格式");
        Assert.isTrue(isWordFile(node.getOutputPath()), "输出文件格式不支持，仅支持.docx格式");
    }

    private boolean isWordFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return false;
        }
        return filePath.toLowerCase().endsWith(".docx");
    }
}
