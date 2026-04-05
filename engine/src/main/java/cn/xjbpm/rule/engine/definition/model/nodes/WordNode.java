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
 * Word文档生成节点
 * 基于poi-tl模板引擎，使用Word模板和数据变量生成新的Word文档
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class WordNode extends Node {

    /**
     * Word模板文件路径
     * 支持相对路径（相对于规则流附件目录）和绝对路径
     */
    private String templatePath;

    /**
     * 输出文件路径
     * 生成的Word文档保存路径
     */
    private String outputPath;

    /**
     * 数据源变量名
     * 指向一个Map类型的变量，key与模板中的占位符对应
     */
    private String dataVariable;

    /**
     * 是否启用SpringEL表达式
     * 启用后可以在模板中使用SpringEL语法
     */
    private boolean useSpringEL = false;

    /**
     * 是否自动创建目录
     */
    private boolean createDirectory = true;

    @Override
    public NodeType getType() {
        return NodeType.WordNode;
    }
}
