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
package cn.xjbpm.rule.engine;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
public class FunctionsTest {

    private static final Map<String, Object> env = JsonUtils.json2Obj("{\"用户\":{\"年龄\":38}}", Map.class);

    // 字符串函数测试
    @Test
    void stringFunctions() {
        // BEGIN_WITH
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("BEGIN_WITH('hello world','hello')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("BEGIN_WITH('hello world','world')", env)));
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("BEGIN_WITH('hello','')", env)));
        
        // END_WITH
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("END_WITH('hello world','world')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("END_WITH('hello world','hello')", env)));
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("END_WITH('hello','')", env)));
        
        // EQUALS
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("EQUALS('hello world','hello world')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("EQUALS('hello world','hello')", env)));

        // IS_EMPTY
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("IS_EMPTY('')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("IS_EMPTY('hello')", env)));
        
        // LOWER
        Assert.assertEquals("hello world", AviatorExecutor.execute(AviatorContext.create("LOWER('Hello World')", env)));

        // STR_LIKE
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("STR_LIKE('hello world','hello.*')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("STR_LIKE('hello world','hi.*')", env)));

        // TRIM
        Assert.assertEquals("hello", AviatorExecutor.execute(AviatorContext.create("TRIM('  hello  ')", env)));
        Assert.assertEquals("", AviatorExecutor.execute(AviatorContext.create("TRIM('')", env)));
        
        // UPPER
        Assert.assertEquals("HELLO WORLD", AviatorExecutor.execute(AviatorContext.create("UPPER('hello world')", env)));
    }
    
    // 数字函数测试
    @Test
    void numberFunctions() {
        // NUMBER_BETWEEN
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("NUMBER_BETWEEN(5,1,10)", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("NUMBER_BETWEEN(15,1,10)", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("NUMBER_BETWEEN(5,10,1)", env)));
        
        // NOT_BETWEEN
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("NOT_BETWEEN(15,1,10)", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("NOT_BETWEEN(5,1,10)", env)));
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("NOT_BETWEEN(5,10,1)", env)));
    }
    
    // 日期函数测试
    @Test
    void dateFunctions() {
        // NOW
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("NOW()", env)));
        
        // TIMESTAMP
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("TIMESTAMP()", env)));
    }
    
    // 集合函数测试
    @Test
    void collectionFunctions() {
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("coll.contains(seq.list(1,2,3),1)", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("coll.contains(seq.list(1,2,3),4)", env)));
        
        // LIST_IS_EMPTY
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("LIST_IS_EMPTY(seq.list())", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("LIST_IS_EMPTY(seq.list(1,2,3))", env)));
    }
    
    // 加密函数测试
    @Test
    void encryptionFunctions() {
        // MD5
        Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", AviatorExecutor.execute(AviatorContext.create("MD5('123456')", env)));

        // SHA256
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("SHA256('123456')", env)));

        // HMACSHA256
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("HMACSHA256('123456','key')", env)));

        // VALUE
        Assert.assertEquals("123456", AviatorExecutor.execute(AviatorContext.create("VALUE('123456')", env)).toString());
    }
    
    // 对象函数测试
    @Test
    void objectFunctions() {
        // IS_NULL
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("IS_NULL('hello')", env)));
        
        // IS_NOT_NULL
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("IS_NOT_NULL('hello')", env)));
    }
    
    // 所得税函数测试
    @Test
    void incomeTaxFunctions() {
        // GET_FAST_DEDUCTION
        Assert.assertEquals(0L, AviatorExecutor.execute(AviatorContext.create("GET_FAST_DEDUCTION(0)", env)));
        Assert.assertEquals(2520L, AviatorExecutor.execute(AviatorContext.create("GET_FAST_DEDUCTION(50000)", env)));
        Assert.assertEquals(16920L, AviatorExecutor.execute(AviatorContext.create("GET_FAST_DEDUCTION(200000)", env)));

        // GET_INCOME_TAX_RATE
        Assert.assertEquals(3L, AviatorExecutor.execute(AviatorContext.create("GET_INCOME_TAX_RATE(0)", env)));
        Assert.assertEquals(10L, AviatorExecutor.execute(AviatorContext.create("GET_INCOME_TAX_RATE(50000)", env)));
        Assert.assertEquals(20L, AviatorExecutor.execute(AviatorContext.create("GET_INCOME_TAX_RATE(200000)", env)));
    }
    
    // 字符串工具函数测试
    @Test
    void stringUtilsFunctions() {
        // str.equals
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.equals('hello','hello')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.equals('hello','world')", env)));

        // str.replace
        Assert.assertEquals("hello-world", AviatorExecutor.execute(AviatorContext.create("str.replace('hello world',' ','-')", env)));

        // str.matches
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.matches('hello world','hello.*')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.matches('hello world','hi.*')", env)));
        
        // str.contains
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.contains('hello world','hello')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.contains('hello world','hi')", env)));
        
        // str.startsWith
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.startsWith('hello world','hello')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.startsWith('hello world','world')", env)));
        
        // str.endsWith
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.endsWith('hello world','world')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.endsWith('hello world','hello')", env)));
        
        // str.trim
        Assert.assertEquals("hello", AviatorExecutor.execute(AviatorContext.create("str.trim('  hello  ')", env)));

        // str.toLowerCase
        Assert.assertEquals("hello world", AviatorExecutor.execute(AviatorContext.create("str.toLowerCase('Hello World')", env)));

        // str.toUpperCase
        Assert.assertEquals("HELLO WORLD", AviatorExecutor.execute(AviatorContext.create("str.toUpperCase('hello world')", env)));

        // str.length
        Assert.assertEquals(11L, AviatorExecutor.execute(AviatorContext.create("str.length('hello world')", env)));

        // str.isEmpty
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.isEmpty('')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.isEmpty('hello')", env)));
        
        // str.isBlank
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.isBlank('')", env)));
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.isBlank('   ')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.isBlank('hello')", env)));
        
        // str.isNotBlank
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.isNotBlank('hello')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.isNotBlank('')", env)));

        // str.isNotEmpty
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.isNotEmpty('hello')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.isNotEmpty('')", env)));

        // str.isNumeric
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("str.isNumeric('123')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("str.isNumeric('abc')", env)));
    }
    
    // 集合工具函数测试
    @Test
    void collUtilsFunctions() {
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("coll.contains(seq.list(1,2,3),1)", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("coll.contains(seq.list(1,2,3),4)", env)));
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("coll.isEmpty(seq.list())", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("coll.isEmpty(seq.list(1,2,3))", env)));
        Assert.assertEquals(3L, AviatorExecutor.execute(AviatorContext.create("coll.size(seq.list(1,2,3))", env)));
    }
    
    // BASE64工具函数测试
    @Test
    void base64Functions() {
        Assert.assertEquals("5rWL6K+V", AviatorExecutor.execute(AviatorContext.create("base64.encode('测试')", env)));
        Assert.assertEquals("测试", AviatorExecutor.execute(AviatorContext.create("base64.decode('5rWL6K+V')", env)));
    }
    
    // URL工具函数测试
    @Test
    void urlFunctions() {
        Assert.assertEquals("https%3A%2F%2Fwww.example.com", AviatorExecutor.execute(AviatorContext.create("url.encode('https://www.example.com')", env)));
        Assert.assertEquals("https://www.example.com", AviatorExecutor.execute(AviatorContext.create("url.decode('https%3A%2F%2Fwww.example.com')", env)));
    }
    
    // 所得税工具函数测试
    @Test
    void taxUtilsFunctions() {
        // tax.fastDeduction
        Assert.assertEquals(0L, AviatorExecutor.execute(AviatorContext.create("tax.fastDeduction(0)", env)));
        Assert.assertEquals(2520L, AviatorExecutor.execute(AviatorContext.create("tax.fastDeduction(50000)", env)));
        Assert.assertEquals(16920L, AviatorExecutor.execute(AviatorContext.create("tax.fastDeduction(200000)", env)));

        // tax.getIncomeTaxRate
        Assert.assertEquals(3L, AviatorExecutor.execute(AviatorContext.create("tax.getIncomeTaxRate(0)", env)));
        Assert.assertEquals(10L, AviatorExecutor.execute(AviatorContext.create("tax.getIncomeTaxRate(50000)", env)));
        Assert.assertEquals(20L, AviatorExecutor.execute(AviatorContext.create("tax.getIncomeTaxRate(200000)", env)));
    }
    
    // 数字工具函数测试
    @Test
    void numUtilsFunctions() {
        // num.add
        Assert.assertEquals(BigDecimal.valueOf(5), AviatorExecutor.execute(AviatorContext.create("num.add(2,3)", env)));

        // num.sub
        Assert.assertEquals(BigDecimal.valueOf(1), AviatorExecutor.execute(AviatorContext.create("num.sub(3,2)", env)));

        // num.mul
        Assert.assertEquals(BigDecimal.valueOf(6), AviatorExecutor.execute(AviatorContext.create("num.mul(2,3)", env)));

        // num.div
        Assert.assertEquals(BigDecimal.valueOf(2), AviatorExecutor.execute(AviatorContext.create("num.div(6,3)", env)));
        try {
            Assert.assertEquals(BigDecimal.valueOf(0), AviatorExecutor.execute(AviatorContext.create("num.div(6,0)", env)));
        }catch (Exception e){
            Assert.assertEquals("num.div -> 除数不能为 0", e.getMessage());
        }

        // num.abs
        Assert.assertEquals(BigDecimal.valueOf(5), AviatorExecutor.execute(AviatorContext.create("num.abs(-5)", env)));
        Assert.assertEquals(BigDecimal.valueOf(5), AviatorExecutor.execute(AviatorContext.create("num.abs(5)", env)));

        // num.round
        Assert.assertEquals(BigDecimal.valueOf(3), AviatorExecutor.execute(AviatorContext.create("num.round(2.5)", env)));
        Assert.assertEquals(BigDecimal.valueOf(2), AviatorExecutor.execute(AviatorContext.create("num.round(2.4)", env)));
        Assert.assertEquals(BigDecimal.valueOf(3), AviatorExecutor.execute(AviatorContext.create("num.round(2.5,0)", env)));
        Assert.assertEquals(BigDecimal.valueOf(2), AviatorExecutor.execute(AviatorContext.create("num.round(2.4,0)", env)));
        // num.max
        Assert.assertEquals(BigDecimal.valueOf(5), AviatorExecutor.execute(AviatorContext.create("num.max(3,5)", env)));

        // num.min
        Assert.assertEquals(BigDecimal.valueOf(3), AviatorExecutor.execute(AviatorContext.create("num.min(3,5)", env)));

        // num.isEven
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("num.isEven(2)", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("num.isEven(3)", env)));

        // num.isOdd
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("num.isOdd(3)", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("num.isOdd(2)", env)));
    }
    
    // 日期工具函数测试
    @Test
    void dateUtilsFunctions() {
        // date.format
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("date.format(date.now(),'yyyy-MM-dd')", env)));
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("date.format(date.now())", env)));

        // date.parse
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("date.parse('2023-01-01','yyyy-MM-dd')", env)));

        // date.diffDays
        Assert.assertEquals(1L, AviatorExecutor.execute(AviatorContext.create("date.diffDays('2023-01-02','2023-01-01','yyyy-MM-dd')", env)));

        // date.addDays
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("date.addDays('2023-01-01',1)", env)));

        // date.isBefore
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("date.isBefore('2023-01-01','2023-01-02','yyyy-MM-dd')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("date.isBefore('2023-01-02','2023-01-01','yyyy-MM-dd')", env)));

        // date.isAfter
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("date.isAfter('2023-01-02','2023-01-01','yyyy-MM-dd')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("date.isAfter('2023-01-01','2023-01-02','yyyy-MM-dd')", env)));

        // date.isToday
        Assert.assertEquals(true,AviatorExecutor.execute(AviatorContext.create("date.isToday(date.now())", env)));
        Assert.assertEquals(false,AviatorExecutor.execute(AviatorContext.create("date.isToday(date.parse('2023-01-01','yyyy-MM-dd'))", env)));
        Assert.assertEquals(true,AviatorExecutor.execute(AviatorContext.create("date.isSameDay(date.now(),date.now())", env)));
        Assert.assertEquals(false,AviatorExecutor.execute(AviatorContext.create("date.isSameDay(date.now(),date.parse('2023-01-01','yyyy-MM-dd'))", env)));
    }
    
    // URL工具函数测试
    @Test
    void urlUtilsFunctions() {
        Assert.assertEquals("https%3A%2F%2Fwww.example.com", AviatorExecutor.execute(AviatorContext.create("url.encode('https://www.example.com')", env)));
        Assert.assertEquals("https://www.example.com", AviatorExecutor.execute(AviatorContext.create("url.decode('https%3A%2F%2Fwww.example.com')", env)));
        Assert.assertEquals(true, AviatorExecutor.execute(AviatorContext.create("url.isValid('https://www.example.com')", env)));
        Assert.assertEquals(false, AviatorExecutor.execute(AviatorContext.create("url.isValid('invalid-url')", env)));
    }
    
    // 加密工具函数测试
    @Test
    void cryptoUtilsFunctions() {
        // crypto.md5
        Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", AviatorExecutor.execute(AviatorContext.create("crypto.md5('123456')", env)));

        // crypto.sha1
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("crypto.sha1('123456')", env)));

        // crypto.sha256
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("crypto.sha256('123456')", env)));

        // crypto.hmacSha256
        Assert.assertNotNull(AviatorExecutor.execute(AviatorContext.create("crypto.hmacSha256('123456','key')", env)));
    }


    @Test
    void convertFunctions() {
        Assert.assertEquals(123456L, AviatorExecutor.execute(AviatorContext.create("long('123456')", env)));
        Assert.assertEquals("123456", AviatorExecutor.execute(AviatorContext.create("str(123456)", env)));
    }

}