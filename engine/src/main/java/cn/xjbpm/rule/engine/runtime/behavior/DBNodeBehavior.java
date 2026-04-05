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

package cn.xjbpm.rule.engine.runtime.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.common.constant.RuleFlowConstant;
import cn.xjbpm.rule.common.utils.DBUtil;
import cn.xjbpm.rule.custom.CredentialsManager;
import cn.xjbpm.rule.engine.aviator.AviatorContext;
import cn.xjbpm.rule.engine.aviator.AviatorExecutor;
import cn.xjbpm.rule.engine.definition.model.nodes.DBNode;
import cn.xjbpm.rule.engine.runtime.model.FlowContext;
import cn.xjbpm.rule.engine.runtime.model.credentials.DataBaseCredential;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Trans;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2022/9/30
 */
@Slf4j
public class DBNodeBehavior implements NodeBehavior {

    private final DBNode node;

    public DBNodeBehavior(DBNode node) {
        this.node = node;
    }

    @Override
    public void execution(FlowContext context) throws Exception {
        if (RuleFlowConstant.DEMO_MODE) {
            context.addTraceLog(node.getId(), "演示模式不允执行,已跳过");
            context.setNodeOutput(node.getId(), Collections.singletonMap(RESULT, "演示模式不允执行"));
            return;
        }

        CredentialsManager credentialsManager = context.getEngineServices().getCredentialsManager();
        DataBaseCredential dataBaseCredential = credentialsManager.getDataBaseCredential(node.getCredentialId());
        Map<String, Object> variable = context.getVariable();
        String rawSql = node.getSql();
        Map<String, Object> parameters = new HashMap<>();
        List<DBNode.Param> params = node.getParams();
        if (CollUtil.isNotEmpty(params)) {
            for (DBNode.Param param : params) {
                AviatorContext aviatorContext = AviatorContext.builder().cached(true)
                        .expression(param.getExpression())
                        .env(variable)
                        .build();
                Object value = AviatorExecutor.execute(aviatorContext);
                if (param.isNullable() == false) {
                    Assert.notNull(value, String.format("参数[%s]不能为空", param.getField()));
                    if (value instanceof Collection collection) {
                        Assert.isTrue(CollUtil.isNotEmpty(collection), String.format("参数[%s]不能为空", param.getField()));
                    }
                    if (value instanceof String str) {
                        Assert.isTrue(StrUtil.isNotBlank(str), String.format("参数[%s]不能为空", param.getField()));
                    }
                }
                parameters.put(param.getField(), value);

            }
        }
        String sql = DBUtil.processDynamicSql(rawSql, parameters);
        Dao dao = dataBaseCredential.getDao();
        if (node.getExecuteType() == DBNode.ExecuteType.UPDATE) {
            // 使用多语句分割（兼容分号）
            List<String> sqls = StrUtil.split(sql, ";").stream().filter(StrUtil::isNotBlank).toList();
            int updateCount;
            if (sqls.size() > 1) {
                AtomicInteger count = new AtomicInteger();
                Trans.exec(() -> {
                    for (String singleSql : sqls) {
                        count.addAndGet(DBUtil.executeSQL(dao, singleSql.trim(), parameters));
                    }
                });
                updateCount = count.get();
            } else {
                updateCount = DBUtil.executeSQL(dao, sql, parameters);
            }
            context.setNodeOutput(node.getId(), new HashMap<>(Map.of(RESULT, updateCount)));
        }
        if (node.getExecuteType() == DBNode.ExecuteType.SELECT_ONE) {
            NutMap record = DBUtil.findRecord(dao, sql, parameters);
            context.setNodeOutput(node.getId(), new HashMap<>(Map.of(RESULT, record)));
        }
        if (node.getExecuteType() == DBNode.ExecuteType.SELECT_LIST) {
            List<NutMap> nutMaps = DBUtil.queryList(dao, sql, parameters, new Pager(1, node.getMaxResultCount()));
            context.setNodeOutput(node.getId(), new HashMap<>(Map.of(RESULT, nutMaps)));
        }
    }


}