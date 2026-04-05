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

package cn.xjbpm.rule.engine.definition.model.nodes;

import cn.xjbpm.rule.engine.definition.model.enums.NodeType;
import lombok.Data;

/**
 * PDF文档生成节点
 * 基于OpenHtmlToPdf组件，使用HTML模板和数据变量生成PDF文档
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class PdfNode extends Node {

    /**
     * HTML内容
     * 直接输入HTML内容（与templatePath二选一）
     */
    private String htmlContent;

    /**
     * HTML模板文件路径
     * 支持相对路径（相对于规则流附件目录）和绝对路径
     * 与htmlContent二选一
     */
    private String templatePath;

    /**
     * 输出文件路径
     * 生成的PDF文档保存路径
     */
    private String outputPath;

    /**
     * 数据源变量名
     * 指向一个Map类型的变量，用于模板变量替换
     */
    private String dataVariable;

    /**
     * 基础URL
     * 用于解析HTML中的相对路径资源（图片、CSS等）
     */
    private String baseUrl;

    /**
     * 是否启用SVG支持
     */
    private boolean enableSvg = true;

    /**
     * 是否启用快速模式
     */
    private boolean enableFastMode = true;

    /**
     * 是否自动创建目录
     */
    private boolean createDirectory = true;

    @Override
    public NodeType getType() {
        return NodeType.PdfNode;
    }
}
