# 星极-规则引擎 XJ-Rule

🔥 **高性能、高并发、可视化的企业级规则引擎系统** 🔥

基于 Java、Akka Actor 模型和 Aviator 表达式引擎构建的灵活、强大的规则引擎，专注于高并发场景下的复杂业务规则定义、组合和执行，为企业提供快速决策和弹性调度能力。

[![GitHub stars](https://img.shields.io/github/stars/threefish/rule-engine.svg?style=social&label=Star)](https://github.com/threefish/rule-engine)
[![GitHub forks](https://img.shields.io/github/forks/threefish/rule-engine.svg?style=social&label=Fork)](https://github.com/threefish/rule-engine)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/threefish/rule-engine/blob/main/LICENSE)

简体中文 | [English](README.md)

**[前端源码获取](https://github.com/threefish/rule-engine-web)**

## 🎯 核心优势

- **🚀 高性能**：基于 Akka Actor 模型的高并发设计，支持大规模规则并行执行
- **🎨 可视化**：提供直观的规则定义和流程编排界面，降低业务人员使用门槛
- **⚡ 高灵活**：支持复杂规则条件组合和多种操作符，适应各种业务场景
- **🔄 高可靠**：完整的错误处理和自动重试机制，确保规则执行的可靠性
- **📊 可监控**：详细的执行日志和监控机制，便于问题定位和性能优化
- **🔧 易扩展**：模块化设计，支持自定义扩展和二次开发
- **🤖 IO节点调度**：支持IO节点的调度能力（AI、SSH、Http等）
- **🎯 精确计算**：基于Aviator表达式引擎的高精度计算，支持Decimal类型确保数值精度

## ✨ 项目特点

- **核心调度：基于 Akka Actor 模型**：利用 Akka 的高并发、异步和容错能力，实现规则流程节点间的并行、分支和聚合的弹性调度
- **表达式核心：基于 Aviator 引擎**：提供高性能的表达式解析和执行，支持复杂的逻辑计算
- **灵活的规则定义**：支持多种操作符、组合方式和条件类型
- **流程驱动**：基于流程模型编排规则执行的逻辑和顺序
- **自动重试机制**：节点执行失败时支持配置化的自动重试策略
- **线程安全的上下文管理**：通过 FlowContext 实现节点间安全的数据共享
- **支持同步和异步执行**：可根据业务需求灵活选择执行模式
- **完整的规则执行日志**：详细记录规则执行过程和结果，便于审计和调试

## 🏗️ 项目结构

## 🛠️ 技术栈


| 技术            | 版本    | 用途                          |
| ----------------- | --------- | ------------------------------- |
| Java            | 1.8     | 主要开发语言                  |
| Spring Boot     | 3.5.7   | 服务框架                      |
| Akka            | 2.6.20  | Actor模型，用于高并发流程调度 |
| Aviator         | 5.3.3   | 高性能表达式解析和执行引擎    |
| Quartz          | 2.5.0   | 定时任务框架                  |
| Spring Data JPA | 3.5.7   | 持久化框架                    |
| MySQL           | 8.0.33  | 数据库                        |
| Hutool          | 5.7.18  | Java工具库                    |
| Lombok          | 1.18.42 | 简化Java代码                  |

### 整体架构

```
rule-engine/
├── engine/             # 规则引擎核心模块
│   ├── common/         # 公共工具类和常量
│   ├── custom/         # 自定义服务
│   ├── engine/         # 引擎核心实现
│   │   ├── aviator/    # Aviator表达式执行器
│   │   ├── rule/       # 规则定义
│   │   └── runtime/    # 运行时服务
│   ├── event/          # 事件定义
│   └── exception/      # 异常定义
└── service/            # 规则引擎服务模块
    ├── api/            # API接口定义
    ├── config/         # 配置类
    ├── job/            # 动态任务
    ├── listener/       # 监听器
    ├── node/           # 节点相关定义
    ├── repository/     # 数据访问层
    ├── runner/         # 启动加载器
    ├── service/        # 业务服务实现
    └── utils/          # 工具类
```

## 🔄 规则引擎工作流程

1. **规则定义**：创建规则对象，设置条件和操作符
2. **流程构建**：定义流程模型，通过网关节点编排串行、并行、分支和聚合逻辑
3. **流程执行**：调用 RuleFlowExcuteService 启动流程实例
4. **Actor 调度**：AkkaRuleFlowScheduler 创建 Actor 控制流程
5. **节点执行**：调度 NodeWorkerActor 执行各个节点
6. **依赖处理**：通过 NodeDependencyBuilder 管理节点间依赖，支持串行和并行执行
7. **汇聚处理**：处理多个并行分支的汇合逻辑
8. **自动重试**：节点执行失败时自动触发配置的重试策略
9. **结果处理**：根据规则执行结果执行相应的动作或输出决策
10. **日志记录**：记录完整的执行日志，便于监控和审计

## 📝 表达式执行

规则引擎使用 Aviator 表达式引擎来执行规则表达式：

- **自动类型转换**：支持数字、字符串等类型的精确计算
- **缓存机制**：支持表达式缓存，提高性能
- **自定义函数**：可扩展的自定义函数支持
- **上下文管理**：提供表达式执行的上下文环境

## 🔌 扩展点

1. **自定义函数**：实现 `AviatorFunction` 接口，添加自定义表达式函数
2. **节点行为**：自定义节点执行行为
3. **规则流模型缓存服务**：实现 `RuleFlowModelCacheService` 接口，自定义规则流模型的缓存策略

## 🔧 部署方式

- **独立部署**：作为独立服务部署，提供 REST API 接口，供其他系统调用
- **嵌入部署**：可嵌入到其他 Java 应用中使用，作为应用的一部分运行
- **集群部署**：支持多实例部署，通过负载均衡实现高可用性和横向扩展

## 📈 性能指标

- **单节点 QPS**：支持每秒数千次规则执行
- **规则执行延迟**：毫秒级响应
- **支持的规则数量**：单个规则流支持数百个规则节点
- **支持的并发请求数**：基于 Akka Actor 模型，支持高并发处理

## 🔍 监控与日志

- **执行日志**：详细记录规则执行过程、参数和结果
- **性能监控**：记录规则执行时间、成功率等指标
- **异常监控**：捕获和记录规则执行过程中的异常
- **审计日志**：记录规则的创建、修改、删除等操作

## 📋 开发规范

- **代码风格**：遵循阿里巴巴 Java 开发规范
- **命名规范**：采用驼峰命名法，清晰表达变量和方法的含义
- **注释规范**：关键代码添加详细注释，便于理解和维护
- **测试规范**：编写单元测试和集成测试，确保代码质量

## 🎯 适用场景

✅ **适用场景（高并发、快速决策、复杂条件）**


| 场景           | 核心特点                                                                 | 示例                                                                                                                            |
| ---------------- | -------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| 实时风险评估   | 需要在毫秒级对大量请求进行多维度、并行判断，以快速做出"是"或"否"的决策。 | 信贷审批： 用户提交贷款申请，引擎同时检查"信用分数 > 600？"、"工作年限 > 1年？"、"负债率 < 50%？"等多个规则，快速决定是否通过。 |
| 复杂的费用核算 | 涉及多个条件分支，计算过程需要复杂的顺序和并行操作。                     | 订单折扣： 计算一个订单的最终价格时，引擎同时跑"会员折扣"、"地区运费"、"满减活动"等规则，并将结果合并得出最终应付金额。         |
| 动态营销推荐   | 根据用户当前的状态或行为，在短时间内触发一系列个性化动作或推荐逻辑。     | App 消息推送： 用户打开 App，引擎快速判断："上次登录时间超过 7 天" 或 "购物车有未付款商品"，立即推送相应的召回消息或优惠券。    |
| 业务规则管理   | 需要将业务规则与代码分离，便于业务人员维护和修改                         | 保险理赔： 根据不同的理赔类型、金额、客户等级等条件，自动计算理赔金额和处理流程                                                 |
| 合规性检查     | 需要对业务数据进行多维度的合规性检查，确保业务操作符合法规要求           | 金融交易： 对每笔交易进行反洗钱、反欺诈、合规性检查，确保交易合法合规                                                           |

❌ **不适用场景（长事务、人工干预、纯数据操作）**


| 场景                 | 核心特点                                                                             | 示例                                                                                                |
| ---------------------- | -------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| 长事务/人工审批流    | 流程涉及需要长时间（几小时到几天）等待人工输入或审批的步骤，Akka 的优势难以发挥。    | 员工报销： 员工提交报销单，流程需要等待财务人员审核、领导签字，耗时较长，且涉及到资源锁定和持久化。 |
| 纯粹的数据存储与查询 | 流程的主要目的是简单地进行 CRUD (增删改查) 操作，没有复杂的业务判断逻辑。            | 数据归档： 定时将一年前的订单记录从在线数据库迁移到历史数据库，核心是数据迁移而非业务决策。         |
| 大规模数据批量ETL    | 针对 TB 级别的数据进行清洗、转换和加载，重点在于数据吞吐量和资源管理，而非流程编排。 | 日志处理： 每晚运行程序，读取上百万条服务器日志，清洗格式，然后统一导入大数据平台。                 |

## 📸 截图展示

以下是规则引擎的主要功能截图：

### 规则引擎界面1

![规则引擎界面](screenshot/规则引擎0.png)

### 规则引擎界面2

![规则引擎界面2](screenshot/规则引擎1.png)

### 规则引擎界面3

![规则引擎界面3](screenshot/规则引擎2.png)

### 规则引擎界面4

![规则引擎界面4](screenshot/规则引擎3.png)

### 规则引擎界面5

![规则引擎界面5](screenshot/规则引擎4.png)

### 规则引擎界面6

![规则引擎界面6](screenshot/规则引擎5.png)

### 规则引擎界面7

![规则引擎界面7](screenshot/规则引擎6.png)

### 规则引擎界面8

![规则引擎界面8](screenshot/规则引擎7.png)

### 条件构造界面

![条件构造界面](screenshot/条件构造.png)

### 表达式编辑界面

![表达式编辑界面](screenshot/编辑表达式.png)

## 🚀 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 5.7+

### 安装与运行

1. **克隆项目**

   ```bash
   git clone https://github.com/threefish/rule-engine.git
   cd rule-engine
   ```
2. **配置数据库**

   - 修改 `service/src/main/resources/application.yml` 文件中的数据库配置
3. **构建项目**

   ```bash
   mvn clean install -DskipTests
   ```
4. **启动服务**

   ```bash
   cd service
   mvn spring-boot:run
   ```
5. **访问前端页面**

   - 控制台会输出前端访问地址，点击访问即可

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
-H "token: YOUR_TOKEN" \
-H "Content-Type: application/json" \
-d '{"appCode": "test", "key": "grsdsjs", "requestId": "1111111", "variables": {"key1": "value1", "key2": "value2"}}' \
https://host:port/openapi/v1/ruleflow/excute
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

- [ ]  支持更多节点类型
- [ ]  支持分布式部署和水平扩展
- [ ]  增强规则执行的实时监控和告警功能
- [ ]  提供规则执行的可视化分析工具

## 📞 联系信息

**作者**：黄川 (huchuc@vip.qq.com)

**GitHub**：https://github.com/threefish/rule-engine

**前端项目**：https://github.com/threefish/rule-engine-web

---

**如果您觉得这个项目有帮助，请给个 Star ⭐ 支持一下！**

---

*持续更新中...*
