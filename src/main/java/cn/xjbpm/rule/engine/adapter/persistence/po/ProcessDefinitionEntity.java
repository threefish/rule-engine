package cn.xjbpm.rule.engine.adapter.persistence.po;

import lombok.Data;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2023/7/22
 */
@Data
public class ProcessDefinitionEntity implements java.io.Serializable {

    private Long id;

    private String key;

    private String definitionContent;

}
