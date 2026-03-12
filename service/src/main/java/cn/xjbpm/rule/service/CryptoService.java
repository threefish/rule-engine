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
package cn.xjbpm.rule.service;

import cn.xjbpm.rule.properties.EncryptionProperties;
import cn.xjbpm.rule.utils.AESCryptoUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
public class CryptoService {

    private final EncryptionProperties props;

    public CryptoService(EncryptionProperties props) {
        this.props = props;
    }

    public String encrypt(String text) {
        if (!shouldProcess(text)) {
            return text;
        }
        return AESCryptoUtil.encrypt(text, props.getSecretKey());
    }

    public String decrypt(String cipherText) {
        if (!shouldProcess(cipherText)) {
            return cipherText;
        }
        try {
            return AESCryptoUtil.decrypt(cipherText, props.getSecretKey());
        } catch (Exception e) {
            // 解密失败回退原值
            return cipherText;
        }
    }

    private boolean shouldProcess(String text) {
        return StringUtils.hasText(text) && props.isEnabled() && StringUtils.hasText(props.getSecretKey());
    }
}