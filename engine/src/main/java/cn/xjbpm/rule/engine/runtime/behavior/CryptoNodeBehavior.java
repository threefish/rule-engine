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

package cn.xjbpm.rule.engine.runtime.behavior;

import cn.xjbpm.rule.common.utils.VariableUtils;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.Algorithm;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.Encoding;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.KeySource;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.Operation;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 加解密节点行为处理器
 * 支持AES、RSA、MD5、SHA256以及国密算法（SM2/SM3/SM4）
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class CryptoNodeBehavior implements NodeBehavior {

    private final CryptoNode node;

    public CryptoNodeBehavior(CryptoNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("执行加解密节点: {}", node.getName());

        String input = getInputData(context);
        String key = getKey(context);
        Charset charset = Charset.forName(node.getCharset());
        Encoding encoding = node.getEncoding();

        String result = processCrypto(input, key, charset, encoding);

        String outputVar = getOutputVariable();
        VariableUtils.setPathVariableByValue(outputVar, result, context.getVariable());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        resultMap.put("algorithm", node.getAlgorithm().name());
        resultMap.put("operation", node.getOperation().name());
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "加解密操作完成: {} {}", 
                    new Object[]{node.getAlgorithm().name(), node.getOperation().name()});
        }
    }

    private String getInputData(FlowContext context) {
        Object value = VariableUtils.getByPathVariable(node.getInputVariable(), context.getVariable());
        return value != null ? value.toString() : null;
    }

    private String getKey(FlowContext context) {
        KeySource keySource = node.getKeySource();
        switch (keySource) {
            case DIRECT:
                return node.getSecretKey();
            case VARIABLE:
                Object value = VariableUtils.getByPathVariable(node.getKeyVariable(), context.getVariable());
                return value != null ? value.toString() : null;
            case CREDENTIAL:
                return getKeyFromCredential(context);
            default:
                return node.getSecretKey();
        }
    }

    private String getKeyFromCredential(FlowContext context) {
        return context.getEngineServices()
                .getCredentialsManager()
                .getCryptoCredential(node.getCredentialId())
                .getSecretKey();
    }

    private String getOutputVariable() {
        String outputVar = node.getOutputVariable();
        if (outputVar == null || outputVar.isEmpty()) {
            return node.getInputVariable();
        }
        return outputVar;
    }

    private String processCrypto(String input, String key, Charset charset, Encoding encoding) {
        Algorithm algorithm = node.getAlgorithm();
        Operation operation = node.getOperation();

        return switch (algorithm) {
            case AES -> processAes(input, key, operation, charset, encoding);
            case SM4 -> processSm4(input, key, operation, charset, encoding);
            case RSA -> processRsa(input, key, operation, charset, encoding);
            case SM2 -> processSm2(input, key, operation, charset, encoding);
            case MD5 -> CryptoUtils.md5Hash(input, charset);
            case SHA256 -> CryptoUtils.sha256Hash(input, charset);
            case SM3 -> CryptoUtils.sm3Hash(input, charset);
        };
    }

    private String processAes(String input, String key, Operation operation, Charset charset, Encoding encoding) {
        if (operation == Operation.ENCRYPT) {
            return CryptoUtils.aesEncrypt(input, key, encoding, charset);
        }
        return CryptoUtils.aesDecrypt(input, key, encoding, charset);
    }

    private String processSm4(String input, String key, Operation operation, Charset charset, Encoding encoding) {
        if (operation == Operation.ENCRYPT) {
            return CryptoUtils.sm4Encrypt(input, key, encoding, charset);
        }
        return CryptoUtils.sm4Decrypt(input, key, encoding, charset);
    }

    private String processRsa(String input, String key, Operation operation, Charset charset, Encoding encoding) {
        return switch (operation) {
            case ENCRYPT -> CryptoUtils.rsaEncrypt(input, key, encoding, charset);
            case DECRYPT -> CryptoUtils.rsaDecrypt(input, key, encoding, charset);
            case SIGN -> CryptoUtils.rsaSign(input, key, encoding, charset);
            case VERIFY -> String.valueOf(CryptoUtils.rsaVerify(input, key, node.getPublicKey(), encoding, charset));
            default -> throw new IllegalArgumentException("RSA不支持该操作: " + operation);
        };
    }

    private String processSm2(String input, String key, Operation operation, Charset charset, Encoding encoding) {
        return switch (operation) {
            case ENCRYPT -> CryptoUtils.sm2Encrypt(input, key, encoding, charset);
            case DECRYPT -> CryptoUtils.sm2Decrypt(input, key, encoding, charset);
            case SIGN -> CryptoUtils.sm2Sign(input, key, encoding, charset);
            case VERIFY -> String.valueOf(CryptoUtils.sm2Verify(input, key, node.getPublicKey(), encoding, charset));
            default -> throw new IllegalArgumentException("SM2不支持该操作: " + operation);
        };
    }
}
