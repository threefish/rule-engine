package cn.xjbpm.rule.engine.runtime.model.credentials;

import lombok.Data;

import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/18
 */
@Data
public class HttpCredential {

    private Map<String, String> querys;
    private Map<String, String> headers;

}
