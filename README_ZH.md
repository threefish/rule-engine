# 星极-规则引擎 XJ-Rule

🔥 **高性能、高并发、可视化的企业级规则引擎系统** 🔥

基于 Java 21、Akka Actor 模型和 Aviator 表达式引擎构建的灵活、强大的规则引擎，专注于高并发场景下的复杂业务规则定义、组合和执行，为企业提供快速决策和弹性调度能力。

[![GitHub stars](https://img.shields.io/github/stars/threefish/rule-engine.svg?style=social&label=Star)](https://github.com/threefish/rule-engine)
[![GitHub forks](https://img.shields.io/github/forks/threefish/rule-engine.svg?style=social&label=Fork)](https://github.com/threefish/rule-engine)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/threefish/rule-engine/blob/main/LICENSE)

简体中文 | [English](README.md)

**[前端源码获取](https://github.com/threefish/rule-engine-web)**

## ✨ 核心特性

- **🚀 高性能调度**：基于 Akka Actor 模型实现高并发、异步和容错的规则流程调度，支持大规模节点并行执行，Worker池动态伸缩
- **🎨 可视化设计**：提供直观的规则定义和流程编排界面，支持流程模型可视化编辑，降低业务人员使用门槛
- **⚡ 灵活规则引擎**：基于 Aviator 表达式引擎，支持复杂条件组合、多种操作符、Decimal高精度计算，提供丰富的自定义函数扩展
- **🔄 高可靠执行**：完整的错误处理策略、自动重试机制、线程安全的 FlowContext 上下文管理，支持同步/异步执行模式
- **📦 丰富节点类型**：提供 30+ 种节点类型，覆盖流程控制、数据处理、规则处理、消息队列、文件操作、AI能力等场景
- **🔌 多种触发方式**：支持手动触发、定时触发（Quartz）、消息队列触发（RabbitMQ/Kafka/MQTT）
- **📊 完善监控体系**：执行日志、追踪日志、调试模式、性能监控、异常监控、审计日志，全方位可观测
- **🔧 易于扩展**：模块化设计，支持自定义节点、函数、凭据扩展，可嵌入或独立部署
- **🔐 安全可靠**：JWT Token认证、API Key授权、规则流级别权限控制、凭据加密存储、敏感数据脱敏、SQL注入防护



## 📦 核心节点类型

系统提供 **30+ 种节点类型**，覆盖常见业务场景：

### 流程控制节点

| 节点类型 | 功能描述 | 特性 |
| --------- | --------- | ---- |
| StartNode | 规则流入口点 | 支持多种触发模式（手动、定时、消息队列） |
| EndNode | 规则流终止点 | 标记流程分支结束 |
| SequenceConnNode | 顺序连接节点 | 支持条件判断 |
| ExclusiveGatewayNode | 排他网关 | 按优先级选择唯一分支执行 |
| ParallelGatewayNode | 并行网关 | 同时触发所有后续分支 |
| InclusiveGatewayNode | 包容网关 | 支持多分支汇聚 |
| LoopNode | 循环节点 | 遍历集合执行子流程 |
| DelayWaitNode | 延时等待节点 | 支持延时执行 |

### 数据处理节点

| 节点类型 | 功能描述 | 技术实现 |
| --------- | --------- | -------- |
| AssignmentNode | 变量赋值操作 | Aviator表达式 |
| DBNode | 数据库操作 | Nutz DAO框架 |
| RedisNode | Redis缓存操作 | Jedis客户端，支持25+操作类型 |
| CSVNode | CSV文件读写 | Hutool工具 |
| ExcelReadNode | Excel读取 | Apache POI |
| ExcelWriteNode | Excel写入 | Apache POI |

### 规则处理节点

| 节点类型 | 功能描述 | 特性 |
| --------- | --------- | ---- |
| RuleSetNode | 规则集判断 | 支持循环、条件分支、动作执行 |
| ScoringCardNode | 评分卡计算 | 支持权重计算、分数汇总 |
| DecisionTablesNode | 决策表匹配 | 表格化规则匹配 |
| DmnDecisionTableNode | DMN决策表 | 支持DMN规范 |

### 接口调用节点

| 节点类型 | 功能描述 | 特性 |
| --------- | --------- | ---- |
| HttpNode | HTTP请求 | 支持多种超时配置、响应条件判断 |
| FunctionNode | 自定义表达式 | Aviator脚本执行 |

### 消息队列节点

| 节点类型 | 功能描述 | 特性 |
| --------- | --------- | ---- |
| RabbitMQNode | RabbitMQ消息发送 | 支持多种交换机类型 |
| KafkaNode | Kafka消息发送 | 支持分区、消息确认模式 |
| MQTTNode | MQTT消息发布 | 支持QoS配置 |

### 文件操作节点

| 节点类型 | 功能描述 | 技术实现 |
| --------- | --------- | -------- |
| FTPNode | FTP文件传输 | Apache Commons Net |
| DownloadFileNode | HTTP文件下载 | - |
| SshNode | SSH远程命令执行 | JSch |
| ShellNode | Shell命令执行 | - |

### 通信节点

| 节点类型 | 功能描述 | 特性 |
| --------- | --------- | ---- |
| EmailNode | 邮件发送 | 支持HTML/纯文本、附件、SSL |

### AI节点

| 节点类型 | 功能描述 | 支持平台 |
| --------- | --------- | -------- |
| AITextNode | AI文本生成 | Deepseek、火山引擎 |
| AIImageNode | AI图像生成 | Gemini、火山引擎 |
| AITTSNode | AI语音合成 | Gemini |



## 🛠️ 技术栈

| 技术            | 版本    | 用途                          |
| ----------------- | --------- | ------------------------------- |
| Java            | 21      | 主要开发语言 (LTS版本)        |
| Spring Boot     | 3.5.7   | 服务框架                      |
| Akka            | 2.6.20  | Actor模型，用于高并发流程调度 |
| Aviator         | 5.4.3   | 高性能表达式解析和执行引擎    |
| Quartz          | -       | 定时任务框架                  |
| Spring Data JPA | 3.5.7   | 持久化框架                    |
| MySQL           | 8.0     | 主数据库                      |
| Redis           | -       | 缓存支持                      |
| RabbitMQ        | 5.20.0  | 消息队列                      |
| Kafka           | 3.7.0   | 消息队列                      |
| MQTT            | 1.2.5   | 物联网消息协议                |
| Hutool          | 5.7.18  | Java工具库                    |
| Lombok          | 1.18.42 | 简化Java代码                  |

## 🔧 部署方式

- **独立部署**：作为独立服务部署，提供 REST API 接口，供其他系统调用
- **嵌入部署**：可嵌入到其他 Java 应用中使用，作为应用的一部分运行
- **集群部署**：支持多实例部署，通过负载均衡实现高可用性和横向扩展



## 📋 开发规范

- **代码风格**：遵循阿里巴巴 Java 开发规范
- **命名规范**：采用驼峰命名法，清晰表达变量和方法的含义
- **注释规范**：关键代码添加详细注释，便于理解和维护
- **测试规范**：编写单元测试和集成测试，确保代码质量

## 🎯 适用场景

✅ **适用场景（高并发、快速决策、复杂条件）**

| 场景 | 核心特点 | 示例 |
| ---- | -------- | ---- |
| 实时风险评估 | 毫秒级多维度并行判断 | 信贷审批：同时检查信用分数、工作年限、负债率等规则 |
| 复杂费用核算 | 多条件分支、复杂计算 | 订单折扣：同时计算会员折扣、地区运费、满减活动 |
| 动态营销推荐 | 短时间触发个性化动作 | App推送：判断登录时间、购物车状态，推送相应消息 |
| 业务规则管理 | 规则与代码分离 | 保险理赔：根据类型、金额、等级自动计算 |
| 合规性检查 | 多维度合规检查 | 金融交易：反洗钱、反欺诈、合规性检查 |

❌ **不适用场景（事务性、人工干预）**

| 场景 | 核心特点 | 示例 |
| ---- | -------- | ---- |
| 人工审批流 | 需要长时间等待人工输入 | 员工报销：等待财务审核、领导签字 |


## 📸 截图展示

### 规则引擎界面

![规则引擎界面](screenshot/规则引擎0.png)
![规则引擎界面2](screenshot/规则引擎1.png)
![规则引擎界面3](screenshot/规则引擎2.png)
![规则引擎界面4](screenshot/规则引擎3.png)
![规则引擎界面5](screenshot/规则引擎4.png)
![规则引擎界面6](screenshot/规则引擎5.png)
![规则引擎界面7](screenshot/规则引擎6.png)
![规则引擎界面8](screenshot/规则引擎7.png)

### 条件构造界面

![条件构造界面](screenshot/条件构造.png)

### 表达式编辑界面

![表达式编辑界面](screenshot/编辑表达式.png)

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+

### 安装与运行

1. **克隆项目**

   ```bash
   git clone https://github.com/threefish/rule-engine.git
   cd rule-engine
   ```

2. **配置数据库**

   修改 `service/src/main/resources/application.yml` 文件中的数据库配置：

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://127.0.0.1:3306/rule
       username: root
       password: 123456
   ```

3. **构建项目**

   ```bash
   mvn clean package -DskipTests
   ```

4. **启动服务**

   ```bash
   java -jar service/target/service-0.0.1-SNAPSHOT.jar
   ```

5. **访问前端页面**

   控制台会输出前端访问地址，点击访问即可

### 核心配置

```yaml
rule:
  akkaSystemName: xj-rule                    # Akka系统名称
  akkaDefaultExecuteTimeoutSeconds: 600      # 默认执行超时(秒)
  akkaGlobalWorkerPoolInitSize: 100          # Worker池初始大小
  akkaGlobalWorkerPoolMaxSize: 2000          # Worker池最大大小
  attachmentPath: /path/to/attachment        # 附件存储路径
```

### 使用示例

**嵌入式方式：执行规则流程**

```java
RuleFlowExcuteService ruleFlowExcuteService = ...; // 获取服务实例
Map<String, Object> variables = new HashMap<>(); // 设置输入变量
variables.put("age", 18);
variables.put("score", 85);

ExcuteRuleFlow request = new ExcuteRuleFlow();  
request.setKey("myRuleKey");  // 规则流程的唯一标识
request.setVariables(variables);  // 输入变量
request.setAsyncExcute(false);  // 同步执行

ExcuteRuleFlowResult result = ruleFlowExcuteService.startFlow(request);

// 处理执行结果
if (result.isSuccess()) {
    System.out.println("规则执行成功！");
    System.out.println("执行结果：" + result.getResponse());
} else {
    System.out.println("规则执行失败：" + result.getErrorMessage());
}
```

**独立部署方式：执行规则流程**

```shell
curl -X POST \
-H "Authorization: YOUR_TOKEN" \
-H "Content-Type: application/json" \
-d '{"key": "myRuleKey", "requestId": "1111111", "variables": {"key1": "value1", "key2": "value2"}}' \
http://host:port/openapi/v1/ruleflow/excute
```

## 🤝 贡献指南

欢迎大家参与贡献，共同完善规则引擎项目！

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 Apache 2.0 许可证，详见 [LICENSE](LICENSE) 文件。

## 🔮 未来规划

- [ ] 支持更多节点类型
- [ ] 支持分布式部署和水平扩展
- [ ] 增强规则执行的实时监控和告警功能
- [ ] 提供规则执行的可视化分析工具

## 📞 联系信息

**作者**：黄川 (huchuc@vip.qq.com)

**GitHub**：https://github.com/threefish/rule-engine

**前端项目**：https://github.com/threefish/rule-engine-web

---

**如果您觉得这个项目有帮助，请给个 Star ⭐ 支持一下！**

---

*持续更新中...*
