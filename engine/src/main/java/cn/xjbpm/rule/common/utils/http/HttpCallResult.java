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
package cn.xjbpm.rule.common.utils.http;

import cn.hutool.core.lang.func.LambdaUtil;
import cn.xjbpm.rule.common.utils.JsonUtils;
import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class HttpCallResult {

    private final int statusCode;
    private final String body;
    private final Map<String,Object> headers;
    private Map jsonBody;

    public HttpCallResult(int statusCode, String body, Map<String,Object> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
        try {
            this.jsonBody = JsonUtils.json2Obj(body, Map.class);
        } catch (Exception e) {
            this.jsonBody = null;
        }
    }

    public boolean is2xx() {
        return statusCode >= 200 && statusCode < 300;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> response = new HashMap<>();
        response.put(LambdaUtil.getFieldName(HttpCallResult::getBody), this.getBody());
        response.put(LambdaUtil.getFieldName(HttpCallResult::getStatusCode), this.getStatusCode());
        response.put(LambdaUtil.getFieldName(HttpCallResult::getJsonBody), this.getJsonBody());
        response.put(LambdaUtil.getFieldName(HttpCallResult::getHeaders), this.getHeaders());
        return response;
    }

}
