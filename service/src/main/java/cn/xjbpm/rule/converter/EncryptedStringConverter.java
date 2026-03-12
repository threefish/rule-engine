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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA 字段加密转换器
 * 自动对敏感字段进行加密存储和解密读取
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return ConverterHolder.getCryptoService().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return ConverterHolder.getCryptoService().decrypt(dbData);
    }
}
