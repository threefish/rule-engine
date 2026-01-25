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
package cn.xjbpm.rule.common.utils;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/23
 */
public class StringUtils {

    public static String format(String template, Object... args) {
        if (template == null || args == null || args.length == 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template.length() + args.length * 10);
        int argIndex = 0;
        int start = 0;
        int i;
        while ((i = template.indexOf("{}", start)) != -1) {
            if (argIndex >= args.length) {
                break;
            }
            sb.append(template, start, i);
            Object arg = args[argIndex++];
            sb.append(arg == null ? "null" : arg);
            start = i + 2;
        }
        sb.append(template, start, template.length());
        return sb.toString();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }
}