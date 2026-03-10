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
 * Excel写入节点
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class ExcelWriteNode extends Node {

    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 工作表名称（不填写则使用第一个工作表）
     */
    private String sheetName;
    /**
     * 写入模式
     */
    private WriteMode writeMode = WriteMode.OVERWRITE;
    /**
     * 数据源变量名
     */
    private String dataVariable;
    /**
     * 起始行（从1开始）
     */
    private int startRow = 1;
    /**
     * 起始列（从1开始）
     */
    private int startCol = 1;
    /**
     * 是否写入表头
     */
    private boolean hasHeader = true;
    /**
     * 列映射（JSON格式）
     */
    private String columnMapping;
    /**
     * 日期格式
     */
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    /**
     * 自动调整列宽
     */
    private boolean autoSizeColumn = true;
    /**
     * 自动创建目录
     */
    private boolean createDirectory = true;

    @Override
    public NodeType getType() {
        return NodeType.ExcelWriteNode;
    }

    public enum WriteMode {
        /**
         * 覆盖工作表
         */
        OVERWRITE,
        /**
         * 追加数据
         */
        APPEND,
        /**
         * 新建工作表
         */
        NEW_SHEET
    }
}
