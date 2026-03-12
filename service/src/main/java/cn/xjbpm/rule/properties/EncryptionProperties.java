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

package cn.xjbpm.rule.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 加密配置属性
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
@ConfigurationProperties(prefix = "rule.encryption")
@Data
public class EncryptionProperties {

    /**
     * AES加密密钥
     * 必须配置，建议使用16/24/32字节的密钥
     * 可使用 AESCryptoUtil.generateKey(32) 生成
     */
    private String secretKey;

    /**
     * 是否启用加密（默认启用）
     */
    private boolean enabled = true;
}
