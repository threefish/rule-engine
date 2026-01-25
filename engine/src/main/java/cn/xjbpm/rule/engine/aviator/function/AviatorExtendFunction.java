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
package cn.xjbpm.rule.engine.aviator.function;

import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Data
public class AviatorExtendFunction {

    private String label;
    private String value;
    private String description;
    private String response;
    private String example;
    private String link;

    public AviatorExtendFunction(String label, String value, String response, String description, String example, String link) {
        this.label = label;
        this.value = value;
        this.response = response;
        this.description = description;
        this.link = link;
        this.example = example;
    }

    public AviatorExtendFunction(String label, String value, String response, String description, String example) {
        this.label = label;
        this.value = value;
        this.response = response;
        this.description = description;
        this.example = example;
    }

    public AviatorExtendFunction(String label, String value, String response, String description) {
        this.label = label;
        this.value = value;
        this.response = response;
        this.description = description;
    }

    public AviatorExtendFunction(String label, String value, String response) {
        this.label = label;
        this.value = value;
        this.response = response;
    }
}