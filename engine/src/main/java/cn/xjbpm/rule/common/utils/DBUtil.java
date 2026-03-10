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

import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.util.NutMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class DBUtil {


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


}