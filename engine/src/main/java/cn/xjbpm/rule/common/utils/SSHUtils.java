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

import cn.xjbpm.rule.engine.runtime.model.credentials.SshCredential;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * SSH/SFTP 工具类
 *
 * @author 黄川 huchuc@vip.qq.com
 */
@Slf4j
public class SSHUtils {

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_TIMEOUT = 60000;
    private static final int ALIVE_INTERVAL = 30000;

    /**
     * 执行命令并自动释放 Session
     */
    public static String execute(SshCredential sshCredential, String command) throws Exception {
        return withSession(sshCredential, session -> execute(session, command));
    }

    /**
     * 基于已有 Session 执行命令，保持方法签名
     */
    private static String execute(Session session, String command) throws Exception {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream in = channel.getInputStream();
            channel.connect();

            StringBuilder result = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                // 读取标准输出
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result.append(new String(tmp, 0, i, StandardCharsets.UTF_8));
                }
                // 检查通道状态
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    int exitStatus = channel.getExitStatus();
                    log.info("SSH命令执行结束, exit-status: {}", exitStatus);
                    if (exitStatus != 0) {
                        // 可以选择性在此处理错误流内容
                        log.warn("命令执行异常返回，status: {}", exitStatus);
                    }
                    break;
                }
                Thread.sleep(100);
            }
            return result.toString();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    /**
     * 核心生命周期管理：自动连接、执行、关闭
     */
    private static <T> T withSession(SshCredential credential, SSHAction<T> action) throws Exception {
        Session session = null;
        try {
            session = createConnectedSession(credential);
            return action.doInSSH(session);
        } catch (JSchException e) {
            handleJSchException(credential, e);
            throw e;
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 统一创建并配置 Session 的逻辑
     */
    private static Session createConnectedSession(SshCredential cred) throws Exception {
        JSch jsch = new JSch();

        // 1. 处理私钥
        if (cred.getPrivateKey() != null && !cred.getPrivateKey().trim().isEmpty()) {
            byte[] prvkey = cred.getPrivateKey().trim().getBytes(StandardCharsets.UTF_8);
            byte[] pass = (cred.getPassphrase() != null) ? cred.getPassphrase().getBytes(StandardCharsets.UTF_8) : null;
            jsch.addIdentity("key-identity", prvkey, null, pass);
        }

        // 2. 创建会话
        Session session = jsch.getSession(cred.getUsername(), cred.getHost(), cred.getPort());
        if (cred.getPassword() != null && !cred.getPassword().isEmpty()) {
            session.setPassword(cred.getPassword());
        }

        // 3. 配置算法与安全策略
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PubkeyAcceptedAlgorithms", "ssh-ed25519,rsa-sha2-512,rsa-sha2-256,ssh-rsa");
        config.put("server_host_key", "ssh-ed25519,ecdsa-sha2-nistp256,rsa-sha2-512,rsa-sha2-256,ssh-rsa");
        config.put("PreferredAuthentications", "publickey,password,keyboard-interactive");
        session.setConfig(config);

        // 4. 设置超时与保活
        session.connect(CONNECT_TIMEOUT);
        session.setTimeout(DEFAULT_TIMEOUT);
        session.setServerAliveInterval(ALIVE_INTERVAL);
        session.setServerAliveCountMax(3);

        return session;
    }

    /**
     * 异常统一翻译
     */
    private static void handleJSchException(SshCredential cred, JSchException e) throws Exception {
        String msg = e.getMessage();
        if (msg != null && msg.contains("Auth fail")) {
            throw new Exception(String.format("SSH认证失败 [%s@%s:%d]: 请检查凭据配置。",
                    cred.getUsername(), cred.getHost(), cred.getPort()), e);
        }
    }

    public static String upload(SshCredential sshCredential, String localPath, String remoteDir, String fileName) throws Exception {
        return withSession(sshCredential, session -> upload(session, localPath, remoteDir, fileName));
    }

    private static String upload(Session session, String localPath, String remoteDir, String fileName) throws Exception {
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            File file = new File(localPath);
            try (InputStream input = new FileInputStream(file)) {
                sftp.cd(remoteDir);
                sftp.put(input, StringUtils.isBlank(fileName) ? file.getName() : fileName);
                log.info("SFTP上传成功: {} -> {}", localPath, remoteDir);
            }
            return String.format("SFTP上传成功: %s -> %s", localPath, remoteDir);
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    public static String download(SshCredential sshCredential, String remotePath, String localDir) throws Exception {
        return withSession(sshCredential, session -> download(session, remotePath, localDir));
    }

    private static String download(Session session, String remotePath, String localDir) throws Exception {
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            File localDirectory = new File(localDir);
            if (!localDirectory.exists() && !localDirectory.mkdirs()) {
                throw new RuntimeException("无法创建本地目录: " + localDir);
            }
            String fileName = new File(remotePath).getName();
            String fullLocalPath = new File(localDirectory, fileName).getAbsolutePath();
            sftp.get(remotePath, fullLocalPath);
            log.info("SFTP下载成功: {} -> {}", remotePath, localDir);
            return String.format("SFTP下载成功: %s -> %s", remotePath, localDir);
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    @FunctionalInterface
    private interface SSHAction<T> {
        T doInSSH(Session session) throws Exception;
    }
}