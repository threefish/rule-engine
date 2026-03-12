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
package cn.xjbpm.rule.converter;

import cn.xjbpm.rule.service.CryptoService;
import org.springframework.stereotype.Component;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Component
public class ConverterHolder {

    private static CryptoService cryptoService;

    public ConverterHolder(CryptoService handler) {
        ConverterHolder.cryptoService = handler;
    }

    public static CryptoService getCryptoService() {
        return cryptoService;
    }
}