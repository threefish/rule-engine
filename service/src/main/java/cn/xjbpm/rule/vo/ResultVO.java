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
package cn.xjbpm.rule.vo;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class ResultVO<T> {

    private T data;
    private String message;
    private Integer code;

    public static <T> ResultVO<T> success(T result) {
        ResultVO resultVO = new ResultVO();
        resultVO.setData(result);
        resultVO.setCode(HttpStatus.OK.value());
        return resultVO;
    }

    public static <T> ResultVO<T> fail(String result) {
        ResultVO resultVO = new ResultVO();
        resultVO.setData(result);
        resultVO.setMessage(result);
        resultVO.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return resultVO;
    }
}