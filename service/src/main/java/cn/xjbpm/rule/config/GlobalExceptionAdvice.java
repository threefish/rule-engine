/*
 * Copyright 2025 threefish.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.config;

import cn.hutool.core.util.StrUtil;
import cn.xjbpm.rule.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/28
 */
@RestControllerAdvice(basePackages = "cn.xjbpm.rule")
@ControllerAdvice(basePackages = "cn.xjbpm.rule")
@Slf4j
public class GlobalExceptionAdvice {

    /**
     * 全局异常捕捉处理
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResultVO exceptionHandler(Exception ex) {
        HttpRequest currentRequest = getCurrentRequest();
        if (currentRequest != null) {
            log.error("当前请求地址:{}", currentRequest.getURI(), ex);
        } else {
            log.error("全局异常", ex);
        }
        return ResultVO.fail(getErrorMsg(ex));
    }

    /**
     * 取得异常message
     *
     * @param ex
     * @return
     */
    private String getErrorMsg(Exception ex) {
        String DUPLICATE_KEY_EXCEPTION = "org.springframework.dao.DuplicateKeyException";
        if (Objects.equals(ex.getClass().getName(), DUPLICATE_KEY_EXCEPTION)) {
            return "违反数据库唯一性约束，请检查数据";
        }
        if (ex instanceof NullPointerException) {
            return "NPE异常";
        }
        String errorMsg = ex.getMessage();
        if (Objects.nonNull(ex.getCause()) && StrUtil.isNotBlank(ex.getCause().getMessage())) {
            errorMsg = ex.getCause().getMessage();
        }
        return errorMsg;
    }

    private HttpRequest getCurrentRequest() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            return requestAttributes instanceof ServletRequestAttributes ? new ServletServerHttpRequest(((ServletRequestAttributes) requestAttributes).getRequest()) : (HttpRequest) requestAttributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 方法参数校验失败异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultVO methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        List<String> errorMessage = new ArrayList<>();
        for (ObjectError error : errors) {
            if (error instanceof FieldError) {
                FieldError fillerError = (FieldError) error;
                String field = fillerError.getField();
                errorMessage.add(String.format("[%s]%s", field, fillerError.getDefaultMessage()));
            } else {
                errorMessage.add(String.format("%s", error.getDefaultMessage()));
            }
        }
        return ResultVO.fail(HttpStatus.BAD_REQUEST.value(), String.join("!", errorMessage));
    }

    /**
     * 方法参数校验失败异常
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public ResultVO methodArgumentNotValidException(IllegalArgumentException ex) {
        return ResultVO.fail(ex.getMessage());
    }
}
