package cn.xjbpm.rule.engine.adapter.persistence;

import cn.xjbpm.rule.engine.adapter.persistence.po.ProcessInstanceEntity;
import org.nutz.dao.enhance.annotation.Dao;
import org.nutz.dao.enhance.dao.BaseDao;
import org.springframework.stereotype.Repository;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Repository
@Dao
public interface ProcessInstanceRepository extends BaseDao<ProcessInstanceEntity> {


}
