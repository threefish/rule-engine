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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            context.put(node.getId(), Collections.singletonMap(RESULT, "演示模式不允执行"));
            return;
        }
        CredentialsManager credentialsManager = context.getBeanContextManager().getCredentialsManager();
        DataBaseCredential dataBaseCredential = credentialsManager.getDataBaseCredential(node.getCredentialId());
        Map<String, Object> variable = context.getVariable();
        String sql = node.getSql();
        Map<String, Object> parameters = new HashMap<>();
        List<DBNode.Param> params = node.getParams();
        if (CollUtil.isNotEmpty(params)) {
            for (DBNode.Param param : params) {
                AviatorContext aviatorContext = AviatorContext.builder().cached(true)
                        .expression(param.getExpression())
                        .env(variable)
                        .build();
                Object value = AviatorExecutor.execute(aviatorContext);
                parameters.put(param.getField(), value);
            }
        }
        Dao dao = dataBaseCredential.getDao();
        if (node.getExecuteType() == DBNode.ExecuteType.UPDATE) {
            int updateCount = DBUtil.executeSQL(dao, sql, parameters);
            context.put(node.getId(), new HashMap<>(Map.of(RESULT, updateCount)));
        }
        if (node.getExecuteType() == DBNode.ExecuteType.SELECT_ONE) {
            NutMap record = DBUtil.findRecord(dao, sql, parameters);
            context.put(node.getId(), new HashMap<>(Map.of(RESULT, record)));
        }
        if (node.getExecuteType() == DBNode.ExecuteType.SELECT_LIST) {
            List<NutMap> nutMaps = DBUtil.queryList(dao, sql, parameters, new Pager(1, node.getMaxResultCount()));
            context.put(node.getId(), new HashMap<>(Map.of(RESULT, nutMaps)));
        }
    }
}