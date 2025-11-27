package cn.xjbpm.rule.service;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.dto.ExcuteRuleFlowVO;
import cn.xjbpm.rule.repository.RuleFlowExcuteLogRepository;
import cn.xjbpm.rule.repository.entity.RuleFlowExcuteLogEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/27
 */
@Service
@AllArgsConstructor
public class RuleFlowExcuteLogService {

    private final RuleFlowExcuteLogRepository ruleFlowExcuteLogRepository;

    /**
     * 保存执行日志
     *
     * @param data
     */
    public void save(ExcuteRuleFlowVO.Response data) {
        RuleFlowExcuteLogEntity log = new RuleFlowExcuteLogEntity();
        log.setId(data.getId());
        log.setRuleFlowKey(data.getRuleFlowKey());
        log.setRequestId(data.getRequestId());
        log.setTimeConsuming(data.getTimeConsuming());
        log.setErrorMessage(data.getErrorMessage());
        log.setSuccess(data.getSuccess());
        log.setContent(JsonUtils.obj2Json(data));
        ruleFlowExcuteLogRepository.save(log);

    }
}
