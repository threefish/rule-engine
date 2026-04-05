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

import java.util.stream.Stream;

/**
 * 加解密节点
 * 支持AES、RSA、MD5、SHA256以及国密算法（SM2/SM3/SM4）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class CryptoNode extends Node {

    /**
     * 加密算法
     */
    private Algorithm algorithm = Algorithm.AES;

    /**
     * 操作类型
     */
    private Operation operation = Operation.ENCRYPT;

    /**
     * 输入数据变量名
     */
    private String inputVariable;

    /**
     * 输出变量名（为空则覆盖输入变量）
     */
    private String outputVariable;

    /**
     * 密钥来源
     */
    private KeySource keySource = KeySource.DIRECT;

    /**
     * 密钥（DIRECT模式）
     */
    private String secretKey;

    /**
     * 密钥变量名（VARIABLE模式）
     */
    private String keyVariable;

    /**
     * 凭据ID（CREDENTIAL模式）
     */
    private String credentialId;

    /**
     * 公钥（RSA/SM2验签时使用）
     */
    private String publicKey;

    /**
     * 私钥（RSA/SM2签名时使用）
     */
    private String privateKey;

    /**
     * 输出编码方式
     */
    private Encoding encoding = Encoding.BASE64;

    /**
     * 字符编码
     */
    private String charset = "UTF-8";

    @Override
    public NodeType getType() {
        return NodeType.CryptoNode;
    }

    /**
     * 加密算法枚举
     */
    public enum Algorithm {
        AES("aes"),
        SM4("sm4"),
        RSA("rsa"),
        SM2("sm2"),
        MD5("md5"),
        SHA256("sha256"),
        SM3("sm3");

        @JsonValue
        private final String value;

        Algorithm(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Algorithm fromValue(String value) {
            return Stream.of(Algorithm.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(AES);
        }
    }

    /**
     * 操作类型枚举
     */
    public enum Operation {
        ENCRYPT("encrypt"),
        DECRYPT("decrypt"),
        SIGN("sign"),
        VERIFY("verify"),
        HASH("hash");

        @JsonValue
        private final String value;

        Operation(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Operation fromValue(String value) {
            return Stream.of(Operation.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(ENCRYPT);
        }
    }

    /**
     * 密钥来源枚举
     */
    public enum KeySource {
        DIRECT("direct"),
        VARIABLE("variable"),
        CREDENTIAL("credential");

        @JsonValue
        private final String value;

        KeySource(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static KeySource fromValue(String value) {
            return Stream.of(KeySource.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(DIRECT);
        }
    }

    /**
     * 输出编码枚举
     */
    public enum Encoding {
        BASE64("base64"),
        HEX("hex");

        @JsonValue
        private final String value;

        Encoding(String value) {
            this.value = value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Encoding fromValue(String value) {
            return Stream.of(Encoding.values())
                    .filter(r -> r.value.equals(value))
                    .findFirst()
                    .orElse(BASE64);
        }
    }
}
