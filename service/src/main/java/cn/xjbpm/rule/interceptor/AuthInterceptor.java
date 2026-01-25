package cn.xjbpm.rule.interceptor;

import cn.xjbpm.rule.common.utils.JsonUtils;
import cn.xjbpm.rule.properties.RuleServiceProperties;
import cn.xjbpm.rule.utils.JwtUtil;
import cn.xjbpm.rule.vo.common.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 权限拦截器内部类
 */
@Service
@AllArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final RuleServiceProperties ruleServiceProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (JwtUtil.validateToken(jwt, ruleServiceProperties.getJwtSecret())) {
                return true;
            }
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(JsonUtils.obj2Json(ResultVO.fail(401, "未授权或Token已过期")));
        }
        return false;
    }
}
