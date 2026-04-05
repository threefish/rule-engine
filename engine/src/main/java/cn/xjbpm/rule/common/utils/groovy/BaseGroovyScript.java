/**
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
package cn.xjbpm.rule.common.utils.groovy;

import cn.xjbpm.rule.common.utils.JsonUtils;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 所有脚本的基类
 */
@SuppressWarnings("all")
public abstract class BaseGroovyScript extends Script {

    /**
     * JSON序列化工具
     */
    protected final static JsonUtils JSON = new JsonUtils();

    protected static final Logger log = LoggerFactory.getLogger(BaseGroovyScript.class);

    /**
     * 当前时间毫秒
     *
     * @return
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 当前纳米时间
     *
     * @return
     */
    protected long nanoTime() {
        return System.nanoTime();
    }

    @Override
    public void println() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void println(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printf(String format, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printf(String format, Object[] values) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object evaluate(String expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evaluate(File file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void run(File file, String[] arguments) {
        throw new UnsupportedOperationException();
    }

}