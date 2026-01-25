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
package cn.xjbpm.rule.utils;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@UtilityClass
public class ApplicationWebPathUtil {

    /**
     * 获取应用访问的根路径
     *
     * @return
     */
    public static String getAppRootPathUrl() {
        String port = StrUtil.nullToDefault(getPort(), "8080");
        if (StrUtil.isNotBlank(port)) {
            port = ":" + port;
        }
        String contextPath = StrUtil.nullToEmpty(getContextPath());
        contextPath = StrUtil.isBlank(contextPath) ? "/" : contextPath;
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }
        String ip = "localhost";
        return String.format("http://%s%s%sindex.html", ip, port, contextPath);
    }

    private static String getContextPath() {
        return SpringContextUtil.getProperty("server.servlet.context-path");
    }

    private static String getPort() {
        return SpringContextUtil.getProperty("server.port");
    }

}
