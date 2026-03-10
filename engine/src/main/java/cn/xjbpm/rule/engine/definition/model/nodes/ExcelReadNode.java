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
 * Excel读取节点
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class ExcelReadNode extends Node {

    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 工作表名称（不填写则读取第一个工作表）
     */
    private String sheetName;
    /**
     * 读取范围类型
     */
    private ReadRangeType readRangeType = ReadRangeType.ALL;
    /**
     * 起始行（从1开始）
     */
    private int startRow = 1;
    /**
     * 结束行（不填则读取到最后一行）
     */
    private Integer endRow;
    /**
     * 起始列（从1开始）
     */
    private int startCol = 1;
    /**
     * 结束列（不填则读取到最后一列）
     */
    private Integer endCol;
    /**
     * 首行为表头
     */
    private boolean hasHeader = true;
    /**
     * 最大读取行数（防止内存溢出）
     */
    private Integer maxRowCount;
    /**
     * 空单元格处理方式
     */
    private EmptyCellHandling emptyCellHandling = EmptyCellHandling.KEEP_NULL;

    @Override
    public NodeType getType() {
        return NodeType.ExcelReadNode;
    }

    public enum ReadRangeType {
        /**
         * 读取全部数据
         */
        ALL,
        /**
         * 指定范围
         */
        RANGE
    }

    public enum EmptyCellHandling {
        /**
         * 保留空值
         */
        KEEP_NULL,
        /**
         * 替换为空字符串
         */
        EMPTY_STRING,
        /**
         * 跳过该行
         */
        SKIP_ROW
    }
}
