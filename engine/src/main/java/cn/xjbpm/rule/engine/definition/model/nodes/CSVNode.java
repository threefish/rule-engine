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
 * CSV节点
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class CSVNode extends Node {

    /**
     * 操作类型
     */
    private OperationType operationType = OperationType.READ;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 分隔符（默认逗号）
     */
    private String delimiter = ",";
    /**
     * 文件编码
     */
    private String encoding = "UTF-8";
    /**
     * 首行为表头
     */
    private boolean hasHeader = true;

    /**
     * 起始行（从1开始，读取时使用）
     */
    private int startRow = 1;
    /**
     * 最大读取行数（防止内存溢出）
     */
    private Integer maxRowCount;

    /**
     * 数据源变量名（写入时使用）
     */
    private String dataVariable;
    /**
     * 追加模式（写入时使用）
     */
    private boolean appendMode;
    /**
     * 写入表头（写入时使用）
     */
    private boolean writeHeader = true;

    @Override
    public NodeType getType() {
        return NodeType.CSVNode;
    }

    public enum OperationType {
        /**
         * 读取CSV
         */
        READ,
        /**
         * 写入CSV
         */
        WRITE
    }
}
