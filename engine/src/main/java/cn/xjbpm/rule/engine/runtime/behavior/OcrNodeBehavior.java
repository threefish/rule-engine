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
import cn.xjbpm.rule.engine.definition.model.nodes.OcrNode;
import cn.xjbpm.rule.engine.definition.model.nodes.OcrNode.ImageSource;
import cn.xjbpm.rule.engine.definition.model.nodes.OcrNode.OcrType;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.OcrCredential;
import com.baidu.aip.ocr.AipOcr;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR识别节点行为处理器
 * 支持百度OCR文字识别服务
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class OcrNodeBehavior implements NodeBehavior {

    private final OcrNode node;

    public OcrNodeBehavior(OcrNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        log.info("执行OCR识别节点: {}, 类型: {}", node.getName(), node.getOcrType());

        OcrCredential credential = context.getEngineServices()
                .getCredentialsManager()
                .getOcrCredential(node.getCredentialId());

        String imageData = getImageData(context);

        AipOcr client = credential.getClient();

        JSONObject result = executeOcr(client, imageData);

        if (result.has("error_code")) {
            String errorCode = result.getString("error_code");
            String errorMsg = result.getString("error_msg");
            log.error("OCR识别失败: errorCode={}, errorMsg={}", errorCode, errorMsg);
            throw new RuntimeException("OCR识别失败: " + errorMsg);
        }

        Map<String, Object> resultMap = parseResult(result);

        String outputVar = getOutputVariable();
        VariableUtils.setPathVariableByValue(outputVar, resultMap, context.getVariable());
        context.setNodeOutput(node.getId(), resultMap);

        if (context.isDebugModel()) {
            context.addTraceLog(node.getId(), "OCR识别完成: {}",
                    new Object[]{node.getOcrType().name()});
        }
    }

    /**
     * 获取图像数据
     */
    private String getImageData(FlowContext context) {
        switch (node.getImageSource()) {
            case BASE64:
                return node.getImageData();
            case URL:
                return node.getImageUrl();
            case VARIABLE:
                Object value = VariableUtils.getByPathVariable(
                        node.getImageData(), context.getVariable());
                return value != null ? value.toString() : null;
            default:
                return node.getImageData();
        }
    }

    /**
     * 执行OCR识别
     */
    private JSONObject executeOcr(AipOcr client, String imageData) {
        HashMap<String, String> options = new HashMap<>();
        options.put("language_type", node.getLanguageType());
        options.put("detect_direction", String.valueOf(node.isDetectDirection()));
        options.put("detect_language", String.valueOf(node.isDetectLanguage()));

        switch (node.getOcrType()) {
            case GENERAL_BASIC:
                if (node.getImageSource() == ImageSource.URL) {
                    return client.basicGeneralUrl(imageData, options);
                }
                return client.basicGeneral(imageData, options);
            case GENERAL_ACCURATE:
                if (node.getImageSource() == ImageSource.URL) {
                    return client.accurateGeneralUrl(imageData, options);
                }
                return client.accurateGeneral(imageData, options);
            case ID_CARD_FRONT:
                return client.idcard(imageData, "front", options);
            case ID_CARD_BACK:
                return client.idcard(imageData, "back", options);
            case BANK_CARD:
                return client.bankcard(imageData, options);
            case INVOICE:
                return client.vatInvoice(imageData, options);
            case TABLE:
                return client.tableRecognitionAsync(imageData, options);
            case DRIVING_LICENSE:
                return client.drivingLicense(imageData, options);
            case VEHICLE_LICENSE:
                return client.vehicleLicense(imageData, options);
            case BUSINESS_LICENSE:
                return client.businessLicense(imageData, options);
            default:
                return client.basicGeneral(imageData, options);
        }
    }

    /**
     * 解析识别结果
     */
    private Map<String, Object> parseResult(JSONObject result) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rawResult", result.toString());
        resultMap.put("ocrType", node.getOcrType().name());

        if (result.has("words_result")) {
            JSONArray wordsResult = result.getJSONArray("words_result");
            List<String> words = new ArrayList<>();
            for (int i = 0; i < wordsResult.length(); i++) {
                JSONObject item = wordsResult.getJSONObject(i);
                words.add(item.getString("words"));
            }
            resultMap.put("words", words);
            resultMap.put("fullText", String.join("\n", words));
            resultMap.put("wordsCount", words.size());
        }

        if (node.getOcrType() == OcrType.ID_CARD_FRONT && result.has("words_result")) {
            JSONArray wordsResult = result.getJSONArray("words_result");
            Map<String, String> idCardInfo = new HashMap<>();
            for (int i = 0; i < wordsResult.length(); i++) {
                JSONObject item = wordsResult.getJSONObject(i);
                if (item.has("location") && item.has("words")) {
                    idCardInfo.put(item.getString("location"), item.getString("words"));
                }
            }
            resultMap.put("idCardInfo", idCardInfo);
        }

        if (result.has("direction")) {
            resultMap.put("direction", result.getInt("direction"));
        }

        return resultMap;
    }

    /**
     * 获取输出变量路径
     */
    private String getOutputVariable() {
        String outputVar = node.getOutputVariable();
        if (outputVar == null || outputVar.isEmpty()) {
            return "ocrResult";
        }
        return outputVar;
    }
}
