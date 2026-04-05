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
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.Algorithm;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.KeySource;
import cn.xjbpm.rule.engine.definition.model.nodes.CryptoNode.Operation;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * 加解密节点验证器
 *
 * @author 黄川 huchuc@vip.qq.com
 */
public class CryptoNodeValidator extends BaseNodeValidator<CryptoNode> {

    private static final Set<Algorithm> HASH_ALGORITHMS = Set.of(Algorithm.MD5, Algorithm.SHA256, Algorithm.SM3);
    private static final Set<Algorithm> ASYMMETRIC_ALGORITHMS = Set.of(Algorithm.RSA, Algorithm.SM2);
    private static final Set<Algorithm> SYMMETRIC_ALGORITHMS = Set.of(Algorithm.AES, Algorithm.SM4);

    @Override
    public void check(CryptoNode node) {
        super.check(node);
        Assert.isTrue(StringUtils.isNotBlank(node.getInputVariable()), "输入数据变量名不能为空");
        validateOperation(node);
        validateKeySource(node);
    }

    private void validateOperation(CryptoNode node) {
        Algorithm algorithm = node.getAlgorithm();
        Operation operation = node.getOperation();

        if (HASH_ALGORITHMS.contains(algorithm)) {
            Assert.isTrue(operation == Operation.HASH, 
                    String.format("算法 %s 只支持 HASH 操作", algorithm.name()));
        }

        if (SYMMETRIC_ALGORITHMS.contains(algorithm)) {
            Assert.isTrue(operation == Operation.ENCRYPT || operation == Operation.DECRYPT,
                    String.format("算法 %s 只支持 ENCRYPT 或 DECRYPT 操作", algorithm.name()));
        }

        if (ASYMMETRIC_ALGORITHMS.contains(algorithm)) {
            Assert.isTrue(operation == Operation.ENCRYPT || operation == Operation.DECRYPT 
                    || operation == Operation.SIGN || operation == Operation.VERIFY,
                    String.format("算法 %s 支持 ENCRYPT、DECRYPT、SIGN 或 VERIFY 操作", algorithm.name()));
        }
    }

    private void validateKeySource(CryptoNode node) {
        if (HASH_ALGORITHMS.contains(node.getAlgorithm())) {
            return;
        }

        KeySource keySource = node.getKeySource();
        switch (keySource) {
            case DIRECT:
                Assert.isTrue(StringUtils.isNotBlank(node.getSecretKey()), 
                        "DIRECT 模式下密钥不能为空");
                break;
            case VARIABLE:
                Assert.isTrue(StringUtils.isNotBlank(node.getKeyVariable()), 
                        "VARIABLE 模式下密钥变量名不能为空");
                break;
            case CREDENTIAL:
                Assert.isTrue(StringUtils.isNotBlank(node.getCredentialId()), 
                        "CREDENTIAL 模式下凭据ID不能为空");
                break;
        }

        if (node.getOperation() == Operation.VERIFY) {
            Assert.isTrue(StringUtils.isNotBlank(node.getPublicKey()), 
                    "验签操作需要提供公钥");
        }
    }
}
