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

package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.stream.Stream;

/**
 * OCR识别节点
 * 支持百度OCR文字识别服务
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OcrNode extends Node {

    /**
     * OCR凭据ID（关联百度OCR的API Key、Secret Key）
     */
    private String credentialId;

    /**
     * 识别类型
     */
    private OcrType ocrType = OcrType.GENERAL_BASIC;

    /**
     * 图像输入源
     */
    private ImageSource imageSource = ImageSource.BASE64;

    /**
     * 图像数据（Base64编码或变量表达式）
     * 支持变量表达式，如 ${imageData}
     */
    private String imageData;

    /**
     * 图像URL地址（当imageSource为URL时使用）
     * 支持变量表达式
     */
    private String imageUrl;

    /**
     * 输出变量路径
     * 识别结果将存储到此变量路径
     */
    private String outputVariable;

    /**
     * 是否检测语言
     */
    private boolean detectLanguage = false;

    /**
     * 是否检测朝向
     */
    private boolean detectDirection = true;

    /**
     * 识别语言类型（默认中英文）
     */
    private String languageType = "CHN_ENG";

    @Override
    public NodeType getType() {
        return NodeType.OcrNode;
    }

    /**
     * OCR识别类型枚举
     */
    public enum OcrType {
        GENERAL_BASIC("general_basic"),
        GENERAL_ACCURATE("general_accurate"),
        GENERAL_ENHANCED("general_enhanced"),
        ID_CARD_FRONT("id_card_front"),
        ID_CARD_BACK("id_card_back"),
        BANK_CARD("bank_card"),
        INVOICE("invoice"),
        TABLE("table"),
        DRIVING_LICENSE("driving_license"),
        VEHICLE_LICENSE("vehicle_license"),
        BUSINESS_LICENSE("business_license");

        @JsonValue
        private final String value;

        OcrType(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static OcrType fromValue(String value) {
            return Stream.of(OcrType.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(GENERAL_BASIC);
        }
    }

    /**
     * 图像输入源枚举
     */
    public enum ImageSource {
        BASE64("base64"),
        URL("url"),
        VARIABLE("variable");

        @JsonValue
        private final String value;

        ImageSource(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static ImageSource fromValue(String value) {
            return Stream.of(ImageSource.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(BASE64);
        }
    }
}
