/**
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
package cn.xjbpm.rule.engine;

import cn.xjbpm.rule.utils.AESCryptoUtil;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2026/3/11
 */
public class AesCryptoTest {
    public static void main(String[] args) {
        String text = AESCryptoUtil.encrypt("{\"username\":\"test\",\"password\":\"test\",\"host\":\"10.201.5.169\",\"port\":1883,\"ssl\":false}", "xj-rule-encryption-secret-key-32byte");
        System.out.println(text);
    }
}