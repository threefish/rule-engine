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
import cn.xjbpm.rule.engine.definition.model.nodes.SshNode;
import org.springframework.util.Assert;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class SshNodeValidator extends BaseNodeValidator<SshNode> {

    @Override
    public void check(SshNode node) {
        super.check(node);
        Assert.notNull(node.getExcuteType(), "执行方式不能为空");
        Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), "链接凭据不能为空");
        switch (node.getExcuteType()) {
            case UPLOAD_FILE:
                Assert.isTrue(StringUtils.isNotBlank(node.getRemoteDirectoryFilePath()), "远程目标目录不能为空");
                Assert.isTrue(StringUtils.isNotBlank(node.getLocalSaveFilePath()), "本地存储文件路径不能为空");
            case DOWNLOAD_FILE:
                Assert.isTrue(StringUtils.isNotBlank(node.getRemoteDirectoryFilePath()), "远程文件路径不能为空");
                Assert.isTrue(StringUtils.isNotBlank(node.getLocalSaveFilePath()), "本地存储文件路径不能为空");
                break;
            case EXECUTE_COMMAND:
                Assert.isTrue(StringUtils.isNotBlank(node.getCommandStr()), "命令不能为空");
                break;
            default:
                break;

        }
    }
}