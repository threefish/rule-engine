/*
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
package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import lombok.Data;

import java.util.List;


/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/28
 */
@Data
public class VolcanoImageGenerationNode extends Node {

    private String credentialId;
    private String modelId;
    private String prompt;
    private String size;
    private ResponseFormat responseFormat;
    private boolean stream;
    private boolean watermark;
    private SequentialImageGeneration sequentialImageGeneration;
    private long sequentialImageGenerationMaxImages;
    private List<Image> images;

    @Override
    public NodeType getType() {
        return NodeType.VolcanoImageGenerationNode;
    }

    public enum ResponseFormat {
        url,
        b64_json
    }

    public enum SequentialImageGeneration {
        auto,
        disabled
    }

    @Data
    public static class Image{
        private String url;
    }

}