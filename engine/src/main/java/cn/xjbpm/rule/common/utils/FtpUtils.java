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

import cn.xjbpm.rule.engine.runtime.model.credentials.FtpCredential;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * FTP 工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class FtpUtils {

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int DATA_TIMEOUT = 60000;
    private static final Charset GBK_CHARSET = Charset.forName("GBK");

    /**
     * 上传文件或目录
     *
     * @param credential   FTP凭据
     * @param localPath    本地路径（文件或目录）
     * @param remotePath   远程路径（文件或目录完整路径）
     * @param passiveMode  被动模式
     * @param binaryMode   二进制模式
     * @param directory    是否为目录操作
     */
    public static String upload(FtpCredential credential, String localPath, String remotePath, boolean passiveMode, boolean binaryMode, boolean directory) throws Exception {
        return withClient(credential, passiveMode, binaryMode, client -> {
            if (directory) {
                return uploadDirectory(client, localPath, remotePath);
            } else {
                return uploadFile(client, localPath, remotePath);
            }
        });
    }

    /**
     * 下载文件或目录
     *
     * @param credential   FTP凭据
     * @param remotePath   远程路径（文件或目录完整路径）
     * @param localPath    本地路径（文件或目录）
     * @param passiveMode  被动模式
     * @param binaryMode   二进制模式
     * @param directory    是否为目录操作
     */
    public static String download(FtpCredential credential, String remotePath, String localPath, boolean passiveMode, boolean binaryMode, boolean directory) throws Exception {
        return withClient(credential, passiveMode, binaryMode, client -> {
            if (directory) {
                return downloadDirectory(client, remotePath, localPath);
            } else {
                return downloadFile(client, remotePath, localPath);
            }
        });
    }

    /**
     * 列出文件
     */
    public static List<String> listFiles(FtpCredential credential, String remoteDir, boolean passiveMode, boolean binaryMode) throws Exception {
        return withClient(credential, passiveMode, binaryMode, client -> listFiles(client, remoteDir));
    }

    /**
     * 删除文件
     */
    public static String delete(FtpCredential credential, String remotePath, boolean passiveMode, boolean binaryMode) throws Exception {
        return withClient(credential, passiveMode, binaryMode, client -> delete(client, remotePath));
    }

    /**
     * 核心生命周期管理：自动连接、执行、关闭
     */
    private static <T> T withClient(FtpCredential credential, boolean passiveMode, boolean binaryMode, FtpAction<T> action) throws Exception {
        FTPClient client = new FTPClient();
        try {
            connect(client, credential, passiveMode, binaryMode);
            return action.doInFtp(client);
        } catch (Exception e) {
            log.error("FTP操作失败: {}", e.getMessage(), e);
            throw e;
        } finally {
            disconnect(client);
        }
    }

    /**
     * 建立连接并配置
     */
    private static void connect(FTPClient client, FtpCredential credential, boolean passiveMode, boolean binaryMode) throws Exception {
        client.setConnectTimeout(CONNECT_TIMEOUT);
        client.setDataTimeout(DATA_TIMEOUT);
        client.setAutodetectUTF8(true);

        int port = credential.getPort() > 0 ? credential.getPort() : 21;
        client.connect(credential.getHost(), port);

        int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new Exception("FTP服务器连接失败: " + reply);
        }

        if (!client.login(credential.getUsername(), credential.getPassword())) {
            throw new Exception("FTP登录失败: 用户名或密码错误");
        }

        if (passiveMode) {
            client.enterLocalPassiveMode();
        } else {
            client.enterLocalActiveMode();
        }

        if (binaryMode) {
            client.setFileType(FTP.BINARY_FILE_TYPE);
        } else {
            client.setFileType(FTP.ASCII_FILE_TYPE);
        }

        setupEncoding(client);

        log.info("FTP连接成功: {}@{}:{}", credential.getUsername(), credential.getHost(), port);
    }

    /**
     * 设置编码，优先尝试UTF-8，不支持则回退到GBK
     */
    private static void setupEncoding(FTPClient client) throws IOException {
        if (client.hasFeature("UTF8") || client.hasFeature("UTF-8")) {
            client.setControlEncoding(StandardCharsets.UTF_8.name());
            client.sendCommand("OPTS UTF8 ON");
            log.debug("FTP服务器支持UTF-8编码");
        } else {
            client.setControlEncoding(GBK_CHARSET.name());
            log.debug("FTP服务器使用GBK编码");
        }
    }

    /**
     * 断开连接
     */
    private static void disconnect(FTPClient client) {
        try {
            if (client.isConnected()) {
                client.logout();
                client.disconnect();
            }
        } catch (IOException e) {
            log.warn("FTP断开连接异常: {}", e.getMessage());
        }
    }

    /**
     * 上传单个文件
     *
     * @param client     FTP客户端
     * @param localPath  本地文件完整路径
     * @param remotePath 远程文件完整路径（包含文件名）
     */
    private static String uploadFile(FTPClient client, String localPath, String remotePath) throws Exception {
        File localFile = new File(localPath);
        if (!localFile.exists()) {
            throw new FileNotFoundException("本地文件不存在: " + localPath);
        }

        String remoteDir = new File(remotePath).getParent();
        if (StringUtils.isNotBlank(remoteDir)) {
            makeDirectories(client, remoteDir);
        }

        try (InputStream input = new FileInputStream(localFile)) {
            if (!client.storeFile(remotePath, input)) {
                throw new Exception("上传文件失败: " + client.getReplyString());
            }
        }

        String message = String.format("FTP上传成功: %s -> %s", localPath, remotePath);
        log.info(message);
        return message;
    }

    /**
     * 上传目录
     */
    private static String uploadDirectory(FTPClient client, String localDir, String remoteDir) throws Exception {
        File localDirectory = new File(localDir);
        if (!localDirectory.exists()) {
            throw new FileNotFoundException("本地目录不存在: " + localDir);
        }
        if (!localDirectory.isDirectory()) {
            throw new IllegalArgumentException("本地路径不是目录: " + localDir);
        }

        int fileCount = uploadDirectoryRecursive(client, localDirectory, remoteDir);
        String message = String.format("FTP上传目录成功: %s -> %s, 共 %d 个文件", localDir, remoteDir, fileCount);
        log.info(message);
        return message;
    }

    /**
     * 递归上传目录
     */
    private static int uploadDirectoryRecursive(FTPClient client, File localDir, String remoteDir) throws Exception {
        makeDirectories(client, remoteDir);

        int fileCount = 0;
        File[] files = localDir.listFiles();
        if (files == null) {
            return fileCount;
        }

        for (File file : files) {
            String remoteFilePath = remoteDir + "/" + file.getName();
            if (file.isDirectory()) {
                fileCount += uploadDirectoryRecursive(client, file, remoteFilePath);
            } else {
                try (InputStream input = new FileInputStream(file)) {
                    if (!client.storeFile(remoteFilePath, input)) {
                        log.warn("上传文件失败: {}", file.getAbsolutePath());
                    } else {
                        fileCount++;
                    }
                }
            }
        }
        return fileCount;
    }

    /**
     * 下载单个文件
     *
     * @param client     FTP客户端
     * @param remotePath 远程文件完整路径
     * @param localPath  本地文件完整路径
     */
    private static String downloadFile(FTPClient client, String remotePath, String localPath) throws Exception {
        File localFile = new File(localPath);
        File parentDir = localFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new RuntimeException("无法创建本地目录: " + parentDir.getAbsolutePath());
        }

        try (OutputStream output = new FileOutputStream(localFile)) {
            if (!client.retrieveFile(remotePath, output)) {
                throw new Exception("下载文件失败: " + client.getReplyString());
            }
        }

        String message = String.format("FTP下载成功: %s -> %s", remotePath, localFile.getAbsolutePath());
        log.info(message);
        return message;
    }

    /**
     * 下载目录
     */
    private static String downloadDirectory(FTPClient client, String remoteDir, String localDir) throws Exception {
        File localDirectory = new File(localDir);
        if (!localDirectory.exists() && !localDirectory.mkdirs()) {
            throw new RuntimeException("无法创建本地目录: " + localDir);
        }

        int fileCount = downloadDirectoryRecursive(client, remoteDir, localDirectory);
        String message = String.format("FTP下载目录成功: %s -> %s, 共 %d 个文件", remoteDir, localDir, fileCount);
        log.info(message);
        return message;
    }

    /**
     * 递归下载目录
     */
    private static int downloadDirectoryRecursive(FTPClient client, String remoteDir, File localDir) throws Exception {
        if (!localDir.exists() && !localDir.mkdirs()) {
            throw new RuntimeException("无法创建本地目录: " + localDir.getAbsolutePath());
        }

        int fileCount = 0;
        FTPFile[] files = client.listFiles(remoteDir);
        if (files == null) {
            return fileCount;
        }

        for (FTPFile ftpFile : files) {
            String fileName = decodeFileName(ftpFile.getName());
            String remoteFilePath = remoteDir + "/" + ftpFile.getName();
            File localFile = new File(localDir, fileName);

            if (ftpFile.isDirectory()) {
                fileCount += downloadDirectoryRecursive(client, remoteFilePath, localFile);
            } else {
                try (OutputStream output = new FileOutputStream(localFile)) {
                    if (client.retrieveFile(remoteFilePath, output)) {
                        fileCount++;
                    }
                }
            }
        }
        return fileCount;
    }

    /**
     * 解码文件名，处理编码问题
     * 如果文件名包含乱码特征，尝试从ISO-8859-1转换为GBK
     */
    private static String decodeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        try {
            byte[] bytes = fileName.getBytes(StandardCharsets.ISO_8859_1);
            if (isGarbled(bytes)) {
                return new String(bytes, GBK_CHARSET);
            }
            return fileName;
        } catch (Exception e) {
            log.warn("文件名解码失败: {}", fileName);
            return fileName;
        }
    }

    /**
     * 判断是否为乱码
     */
    private static boolean isGarbled(byte[] bytes) {
        for (byte b : bytes) {
            if ((b & 0x80) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 列出文件
     */
    private static List<String> listFiles(FTPClient client, String remoteDir) throws Exception {
        FTPFile[] files = client.listFiles(remoteDir);
        List<String> fileNames = new ArrayList<>();
        for (FTPFile file : files) {
            fileNames.add(decodeFileName(file.getName()));
        }
        log.info("FTP列出文件成功: {} 共 {} 个文件", remoteDir, fileNames.size());
        return fileNames;
    }

    /**
     * 删除文件
     */
    private static String delete(FTPClient client, String remotePath) throws Exception {
        if (!client.deleteFile(remotePath)) {
            throw new Exception("删除文件失败: " + client.getReplyString());
        }
        String message = String.format("FTP删除文件成功: %s", remotePath);
        log.info(message);
        return message;
    }

    /**
     * 创建远程目录（支持多级目录）
     */
    private static boolean makeDirectories(FTPClient client, String path) throws IOException {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        String[] dirs = path.split("/");
        String currentPath = "";
        for (String dir : dirs) {
            if (StringUtils.isBlank(dir)) {
                continue;
            }
            currentPath += "/" + dir;
            if (!client.changeWorkingDirectory(currentPath)) {
                if (!client.makeDirectory(currentPath)) {
                    log.warn("无法创建目录: {}", currentPath);
                }
            }
        }
        return true;
    }

    @FunctionalInterface
    private interface FtpAction<T> {
        T doInFtp(FTPClient client) throws Exception;
    }
}
