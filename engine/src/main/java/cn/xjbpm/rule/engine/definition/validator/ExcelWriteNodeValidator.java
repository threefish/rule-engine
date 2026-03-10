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
import cn.xjbpm.rule.engine.definition.model.nodes.ExcelWriteNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class ExcelWriteNodeValidator extends BaseNodeValidator<ExcelWriteNode> {

    @Override
    public void check(ExcelWriteNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getFilePath()), "文件路径不能为空");
        Assert.isTrue(isExcelFile(node.getFilePath()), "文件格式不支持，仅支持.xlsx和.xls格式");
        Assert.isTrue(StringUtils.isNotBlank(node.getDataVariable()), "数据源变量名不能为空");

        if (node.getWriteMode() != ExcelWriteNode.WriteMode.APPEND) {
            Assert.isTrue(node.getStartRow() >= 1, "起始行必须大于等于1");
            Assert.isTrue(node.getStartCol() >= 1, "起始列必须大于等于1");
        }
    }

    private boolean isExcelFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return false;
        }
        String lowerCasePath = filePath.toLowerCase();
        return lowerCasePath.endsWith(".xlsx") || lowerCasePath.endsWith(".xls");
    }
}
