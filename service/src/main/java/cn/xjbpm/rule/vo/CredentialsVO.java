package cn.xjbpm.rule.vo;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.common.utils.StringUtils;
import cn.xjbpm.rule.repository.entity.CredentialsEntity;
import cn.xjbpm.rule.repository.enums.CredentialsType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/17
 */
public class CredentialsVO {


    @Data
    public static class PageQuery {
        private String name;
        private CredentialsType type;
    }

    @Data
    public static class Save {

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long id;

        @NotNull
        private CredentialsType type;

        @NotBlank
        private String name;

        @NotBlank
        private String authData;

    }

    @Data
    public static class PageResponse {

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long id;

        private String name;

        private CredentialsType type;


        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime createTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime updateTime;

        public static PageResponse createPageView(CredentialsEntity entity) {
            if (entity == null) {
                return null;
            }
            PageResponse response = new PageResponse();
            response.setId(entity.getId());
            response.setName(entity.getName());
            response.setType(entity.getType());
            response.setCreateTime(entity.getCreateTime());
            response.setUpdateTime(entity.getUpdateTime());
            return response;
        }
    }

    @Data
    public static class Details {

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long id;

        private String name;

        private CredentialsType type;

        private String authData;

        private Map<String, Object> authDataMap;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime createTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime updateTime;

        public static Details createDetailsView(CredentialsEntity credentialsEntity) {
            if (credentialsEntity == null) {
                return null;
            }
            Details details = new Details();
            details.setId(credentialsEntity.getId());
            details.setName(credentialsEntity.getName());
            details.setType(credentialsEntity.getType());
            details.setAuthData(credentialsEntity.getAuthData());
            details.setCreateTime(credentialsEntity.getCreateTime());
            details.setUpdateTime(credentialsEntity.getUpdateTime());
            if (StringUtils.isNotBlank(credentialsEntity.getAuthData())) {
                details.setAuthDataMap(JsonUtils.json2Obj(credentialsEntity.getAuthData(), Map.class));
            } else {
                details.setAuthDataMap(new HashMap<>());
            }
            return details;
        }
    }

    @Data
    public static class SelectQuery {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long id;
        private String keyword;
        private String type;
        private List<CredentialsType> types;
    }
}
