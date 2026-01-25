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
package cn.xjbpm.rule.engine.runtime.behavior.model;

import lombok.Data;

import java.io.File;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Data
public class FileModel implements java.io.Serializable {

    private String name;
    private String path;
    private long size;
    private boolean exists;
    private boolean file;
    private boolean directory;

    public static FileModel of(File file) {
        FileModel fileModel = new FileModel();
        fileModel.name = file.getName();
        fileModel.path = file.getAbsolutePath();
        fileModel.size = file.length();
        fileModel.exists = file.exists();
        fileModel.file = file.isFile();
        fileModel.directory = file.isDirectory();
        return fileModel;
    }
}
