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

package cn.xjbpm.rule.common.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 一个简单的类扫描器，用于递归扫描指定包路径下的所有类。
 */
public class ClassScanner {

    private final String packageName;
    private final ClassLoader classLoader;

    /**
     * @param packageName 需要扫描的包名，例如 "com.example.functions"
     */
    public ClassScanner(String packageName) {
        this.packageName = packageName;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * 执行类扫描。
     *
     * @return 扫描到的所有 Class 对象的集合。
     */
    public Set<Class<?>> scan() {
        Set<Class<?>> classes = new HashSet<>();
        // 将包名转换为路径，例如 com.example.xxx -> com/example/xxx
        String packageDirName = packageName.replace('.', '/');

        try {
            // 获取在类路径中与给定资源匹配的所有资源的 URL
            Enumeration<URL> resources = classLoader.getResources(packageDirName);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    // 扫描文件系统中的类 (开发环境)
                    String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
                    findClassesInPackageByFile(packageName, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    // 扫描 JAR 包中的类 (部署环境)
                    findClassesInPackageByJar(resource, packageName, packageDirName, classes);
                }
            }
        } catch (IOException e) {
            // 扫描时发生错误
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 递归扫描文件系统中的类文件
     */
    private void findClassesInPackageByFile(String packageName, String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        // 遍历包下的所有文件和目录
        File[] dirfiles = dir.listFiles(file -> (file.isDirectory() || file.getName().endsWith(".class")));

        if (dirfiles != null) {
            for (File file : dirfiles) {
                if (file.isDirectory()) {
                    // 递归扫描子包
                    findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
                } else {
                    // 是 .class 文件，加载类
                    String className = file.getName().substring(0, file.getName().length() - 6); // 去掉 ".class"
                    try {
                        String fullClassName = packageName + '.' + className;
                        // 使用 ClassLoader 加载类
                        classes.add(classLoader.loadClass(fullClassName));
                    } catch (ClassNotFoundException e) {
                        // 无法加载类 (通常不会发生，除非文件路径出错)
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 扫描 JAR 包中的类文件
     */
    private void findClassesInPackageByJar(URL resource, String packageName, String packageDirName, Set<Class<?>> classes) throws IOException {
        JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
        JarFile jarFile = jarURLConnection.getJarFile();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            // 确保是 class 文件且位于目标包或其子包下
            if (name.endsWith(".class") && name.startsWith(packageDirName)) {
                // 将路径名 com/example/xxx/MyClass.class 转换为类名 com.example.xxx.MyClass
                String className = name.substring(0, name.length() - 6).replace('/', '.');

                // 仅加载目标包及其子包中的类
                if (className.startsWith(packageName)) {
                    try {
                        classes.add(classLoader.loadClass(className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}