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

package cn.xjbpm.rule.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.util.NutMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class DBUtil {
    // 匹配 #[ ... ] 语法的正则
    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("\\#\\[([\\s\\S]*?)\\]");
    // 匹配 @param 语法的正则
    private static final Pattern PARAM_PATTERN = Pattern.compile("@(\\w+)");

    public static int executeSQL(Dao dao, String sqlString, Map<String, Object> params) {
        Sql sql = Sqls.create(sqlString);
        sql.setVars(params);
        sql.setParams(params);
        dao.execute(sql);
        return sql.getUpdateCount();
    }


    public static NutMap findRecord(Dao dao, String sqlString, Map<String, Object> params) {
        Sql sql = Sqls.create(sqlString);
        sql.setCallback(Sqls.callback.map());
        sql.setVars(params);
        sql.setParams(params);
        dao.execute(sql);
        return sql.getObject(NutMap.class);
    }

    /**
     * 查询
     *
     * @return
     */
    public static List<NutMap> queryList(Dao dao, String sqlstr, Map<String, Object> params, Condition condition) {
        Sql sql = Sqls.create(sqlstr);
        sql.setCallback(Sqls.callback.maps());
        sql.setVars(params);
        sql.setParams(params);
        sql.setCondition(condition);
        dao.execute(sql);
        List<NutMap> list = sql.getList(NutMap.class);
        return list == null ? Collections.emptyList() : list;
    }

    public static List<NutMap> queryList(Dao dao, String sqlstr, Map<String, Object> params, Pager page) {
        Sql sql = Sqls.create(sqlstr);
        sql.setCallback(Sqls.callback.maps());
        sql.setVars(params);
        sql.setParams(params);
        sql.setPager(page);
        dao.execute(sql);
        List<NutMap> list = sql.getList(NutMap.class);
        return list == null ? Collections.emptyList() : list;
    }


    /**
     * 动态 SQL 解析引擎
     */
    public static String processDynamicSql(String sql, Map<String, Object> params) {
        if (StrUtil.isBlank(sql)) {
            return sql;
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = DYNAMIC_PATTERN.matcher(sql);
        int lastEnd = 0;

        while (matcher.find()) {
            // 添加匹配块之前的静态部分
            sb.append(sql, lastEnd, matcher.start());

            String blockContent = matcher.group(1); // 获取 #[ ] 内部的内容
            if (isBlockValid(blockContent, params)) {
                sb.append(blockContent); // 参数有效，保留内容（去掉 #[]）
            }
            lastEnd = matcher.end();
        }
        sb.append(sql.substring(lastEnd));
        return sb.toString();
    }

    /**
     * 判断动态块内的参数是否全部有效
     */
    private static boolean isBlockValid(String content, Map<String, Object> params) {
        Matcher paramMatcher = PARAM_PATTERN.matcher(content);
        while (paramMatcher.find()) {
            String paramName = paramMatcher.group(1);
            Object value = params.get(paramName);
            // 核心判断逻辑：null 或 空集合 则视为无效
            if (value == null) {
                return false;
            }
            if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                return false;
            }
            if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
                return false;
            }
            if (value instanceof String && ((String) value).isEmpty()) {
                return false;
            }
        }
        // 如果块内没有 @ 参数，默认保留内容；如果有参数且都通过了校验，返回 true
        return true;
    }


}