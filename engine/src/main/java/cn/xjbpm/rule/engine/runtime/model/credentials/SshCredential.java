package cn.xjbpm.rule.engine.runtime.model.credentials;

import lombok.Data;

import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/18
 */
@Data
public class SshCredential {

    private String host;
    private int port;
    private String username;
    private String password;
    private String privateKey;
    private String passphrase;

}
