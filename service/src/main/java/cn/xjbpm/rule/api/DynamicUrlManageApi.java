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

package cn.xjbpm.rule.api;

import cn.xjbpm.rule.repository.entity.DynamicUrlMappingEntity;
import cn.xjbpm.rule.service.DynamicUrlMappingService;
import cn.xjbpm.rule.vo.DynamicUrlMappingVO;
import cn.xjbpm.rule.vo.common.IDRequestVO;
import cn.xjbpm.rule.vo.common.PageVO;
import cn.xjbpm.rule.vo.common.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 黄川 huchuc@vip.qq.com
 * 动态URL映射管理API
 */
@RestController
@RequestMapping("/manage/dynamic-url")
@RequiredArgsConstructor
@Slf4j
public class DynamicUrlManageApi {

    private final DynamicUrlMappingService dynamicUrlMappingService;

    @PostMapping("/add")
    public ResultVO<Boolean> add(@Validated @RequestBody DynamicUrlMappingVO request) {
        return ResultVO.success(dynamicUrlMappingService.add(request));
    }

    @PostMapping("/update")
    public ResultVO<Boolean> update(@Validated @RequestBody DynamicUrlMappingVO request) {
        return ResultVO.success(dynamicUrlMappingService.update(request));
    }

    @PostMapping("/delete")
    public ResultVO<Boolean> delete(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(dynamicUrlMappingService.delete(request.getId()));
    }

    @PostMapping("/enable")
    public ResultVO<Boolean> enable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(dynamicUrlMappingService.enable(request.getId()));
    }

    @PostMapping("/disable")
    public ResultVO<Boolean> disable(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(dynamicUrlMappingService.disable(request.getId()));
    }

    @PostMapping("/get")
    public ResultVO<DynamicUrlMappingVO> get(@Validated @RequestBody IDRequestVO request) {
        return ResultVO.success(dynamicUrlMappingService.findById(request.getId()));
    }

    @PostMapping("/page")
    public ResultVO<PageVO<DynamicUrlMappingVO>> page(
            org.springframework.data.domain.Pageable pageable,
            @RequestBody DynamicUrlMappingVO.PageQuery query) {
        Page<DynamicUrlMappingEntity> page = dynamicUrlMappingService.findPage(
                query.getUrlPath(),
                query.getHttpMethod(),
                query.getRuleFlowKey(),
                query.getStatus(),
                pageable
        );
        List<DynamicUrlMappingVO> voList = page.getContent().stream()
                .map(DynamicUrlMappingVO::create)
                .collect(Collectors.toList());
        return ResultVO.success(PageVO.of(page, voList));
    }

}
