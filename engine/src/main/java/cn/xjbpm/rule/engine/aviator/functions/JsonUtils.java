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

package cn.xjbpm.rule.engine.aviator.functions;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.common.utils.JsonPathUtil;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionDoc;
import cn.xjbpm.rule.engine.aviator.annotation.FunctionNamespace;
import com.jayway.jsonpath.DocumentContext;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@FunctionNamespace(name = "json")
@SuppressWarnings("all")
public class JsonUtils {

    @FunctionDoc(value = "json.toJson(var1)", description = "将对象转为JSON字符串。", example = "json.parseJson(var1)")
    public static String toJson(Object var1) {
        return cn.xjbpm.rule.common.utils.JsonUtils.obj2Json(var1);
    }

    @FunctionDoc(value = "json.json2Obj(text)", description = "将JSON字符串转为对象。文本为空将抛出异常。", example = "json.json2Obj(text)")
    public static Map json2Obj(String text) {
        Assert.isTrue(StrUtil.isNotBlank(text), "json.json2Obj -> text 不能为空");
        return cn.xjbpm.rule.common.utils.JsonUtils.json2Obj(text.toString(), Map.class);
    }

    @FunctionDoc(value = "json.json2List(text)", description = "将JSON字符串转为List对象。文本为空将抛出异常。", example = "json.json2Obj(text)")
    public static List<Object> json2List(String text) {
        Assert.isTrue(StrUtil.isNotBlank(text), "json.json2List -> text 不能为空");
        return cn.xjbpm.rule.common.utils.JsonUtils.json2List(text.toString(), Object.class);
    }

    @FunctionDoc(value = "json.readByJsonPath(jsonPath,text)", description = "通过jsonPath读取JSON数据。", example = "json.readByJsonPath(jsonPath,text)")
    public static Object readByJsonPath(String jsonPath, String text) {
        Assert.isTrue(StrUtil.isNotBlank(text), "json.readByJsonPath -> jsonPath 不能为空");
        Assert.isTrue(StrUtil.isNotBlank(text), "json.readByJsonPath -> text 不能为空");
        DocumentContext parse = JsonPathUtil.parse(text);
        Object read = parse.read(jsonPath, Object.class);
        Assert.notNull(read, String.format("JsonPath读取结果为空:%s", jsonPath));
        return read;
    }

    public static void main(String[] args) {
        Object object = JsonUtils.json2Obj("{\"a\":\"1\"}");
        System.out.println(JsonUtils.json2Obj("{\"a\":\"1\"}"));
        List<Object> objects = JsonUtils.json2List("[\"1\"]");
        System.out.println(objects);
        Object oo = JsonUtils.readByJsonPath("$.a", "{\"a\":\"1\"}");
        System.out.println(oo);
    }

}