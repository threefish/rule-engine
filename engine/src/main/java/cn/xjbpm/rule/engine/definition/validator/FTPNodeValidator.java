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
import cn.xjbpm.rule.engine.definition.model.nodes.FTPNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
public class FTPNodeValidator extends BaseNodeValidator<FTPNode> {

    @Override
    public void check(FTPNode node) {
        super.check(node);
        Assert.notNull(node.getOperationType(), "操作类型不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), "链接凭据不能为空");
        switch (node.getOperationType()) {
            case UPLOAD:
                Assert.isTrue(StringUtils.isNotBlank(node.getRemotePath()), "远程路径不能为空");
                Assert.isTrue(StringUtils.isNotBlank(node.getLocalPath()), "本地路径不能为空");
                validatePathFormat(node.getRemotePath(), node.isDirectory(), "远程路径");
                validatePathFormat(node.getLocalPath(), node.isDirectory(), "本地路径");
                break;
            case DOWNLOAD:
                Assert.isTrue(StringUtils.isNotBlank(node.getRemotePath()), "远程路径不能为空");
                Assert.isTrue(StringUtils.isNotBlank(node.getLocalPath()), "本地路径不能为空");
                validatePathFormat(node.getRemotePath(), node.isDirectory(), "远程路径");
                validatePathFormat(node.getLocalPath(), node.isDirectory(), "本地路径");
                break;
            case LIST:
                Assert.isTrue(StringUtils.isNotBlank(node.getRemotePath()), "远程目录路径不能为空");
                break;
            case DELETE:
                Assert.isTrue(StringUtils.isNotBlank(node.getRemotePath()), "远程文件路径不能为空");
                break;
            default:
                break;
        }
    }

    /**
     * 校验路径格式
     *
     * @param path      路径
     * @param directory 是否为目录
     * @param pathName  路径名称（用于错误提示）
     */
    private void validatePathFormat(String path, boolean directory, String pathName) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        if (directory) {
            Assert.isTrue(isDirectoryPath(path), pathName + "应为目录格式（以/结尾）");
        } else {
            Assert.isTrue(isFilePath(path), pathName + "应为文件格式（包含文件名且不以/结尾）");
        }
    }

    /**
     * 判断是否为目录路径格式
     */
    private boolean isDirectoryPath(String path) {
        String trimmed = path.trim();
        if (StringUtils.isBlank(trimmed)) {
            return false;
        }
        return trimmed.endsWith("/") || trimmed.endsWith("\\");
    }

    /**
     * 判断是否为文件路径格式
     */
    private boolean isFilePath(String path) {
        String trimmed = path.trim();
        if (StringUtils.isBlank(trimmed)) {
            return false;
        }
        if (trimmed.endsWith("/") || trimmed.endsWith("\\")) {
            return false;
        }
        int lastSeparator = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
        String fileName = lastSeparator >= 0 ? trimmed.substring(lastSeparator + 1) : trimmed;
        return StringUtils.isNotBlank(fileName) && fileName.contains(".");
    }
}
