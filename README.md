# XJ-Rule Engine

🔥 **High-performance, High-concurrency, Visualized Enterprise-level Rule Engine System** 🔥

A flexible and powerful rule engine built on Java 21, Akka Actor model, and Aviator expression engine, focusing on complex business rule definition, combination, and execution in high-concurrency scenarios, providing enterprises with fast decision-making and elastic scheduling capabilities.

[![GitHub stars](https://img.shields.io/github/stars/threefish/rule-engine.svg?style=social&label=Star)](https://github.com/threefish/rule-engine)
[![GitHub forks](https://img.shields.io/github/forks/threefish/rule-engine.svg?style=social&label=Fork)](https://github.com/threefish/rule-engine)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/threefish/rule-engine/blob/main/LICENSE)

English | [简体中文](README_ZH.md)

**[Frontend Source Code](https://github.com/threefish/rule-engine-web)**

## ✨ Core Features

- **🚀 High-Performance Scheduling**: Based on Akka Actor model for high-concurrency, asynchronous, and fault-tolerant rule flow scheduling, supporting large-scale parallel node execution with dynamic Worker pool scaling (initial 100, max 2000)
- **🎨 Visual Design**: Provides intuitive rule definition and process orchestration interface, supports visual editing of process models, lowering the barrier for business users
- **⚡ Flexible Rule Engine**: Based on Aviator expression engine, supports complex condition combinations, multiple operators, Decimal high-precision calculation, with rich custom function extensions
- **🔄 High-Reliability Execution**: Complete error handling strategies, automatic retry mechanism, thread-safe FlowContext context management, supports synchronous/asynchronous execution modes
- **📦 Rich Node Types**: Provides 30+ node types, covering process control, data processing, rule processing, message queues, file operations, AI capabilities, and more
- **🔌 Multiple Trigger Methods**: Supports manual trigger, scheduled trigger (Quartz), message queue trigger (RabbitMQ/Kafka/MQTT)
- **📊 Comprehensive Monitoring**: Execution logs, trace logs, debug mode, performance monitoring, exception monitoring, audit logs, full observability
- **🔧 Easy to Extend**: Modular design, supports custom node, function, and credential extensions, can be embedded or independently deployed
- **🔐 Secure and Reliable**: JWT Token authentication, API Key authorization, rule flow level permission control, credential encryption storage, sensitive data masking, SQL injection protection

## 📦 Core Node Types

The system provides **30+ node types**, covering common business scenarios:

### Process Control Nodes

| Node Type | Description | Features |
| --------- | ----------- | -------- |
| StartNode | Rule flow entry point | Supports multiple trigger modes (manual, scheduled, message queue) |
| EndNode | Rule flow termination point | Marks process branch end |
| SequenceConnNode | Sequence connection node | Supports condition judgment |
| ExclusiveGatewayNode | Exclusive gateway | Selects the first matching branch by priority |
| ParallelGatewayNode | Parallel gateway | Triggers all subsequent branches simultaneously |
| InclusiveGatewayNode | Inclusive gateway | Supports multi-branch convergence |
| LoopNode | Loop node | Iterates over collection to execute sub-process |
| DelayWaitNode | Delay wait node | Supports delayed execution |

### Data Processing Nodes

| Node Type | Description | Implementation |
| --------- | ----------- | -------------- |
| AssignmentNode | Variable assignment | Aviator expression |
| DBNode | Database operations | Nutz DAO framework |
| RedisNode | Redis cache operations | Jedis client, supports 25+ operation types |
| CSVNode | CSV file read/write | Hutool utility |
| ExcelReadNode | Excel reading | Apache POI |
| ExcelWriteNode | Excel writing | Apache POI |

### Rule Processing Nodes

| Node Type | Description | Features |
| --------- | ----------- | -------- |
| RuleSetNode | Rule set judgment | Supports loop, conditional branch, action execution |
| ScoringCardNode | Scoring card calculation | Supports weight calculation, score aggregation |
| DecisionTablesNode | Decision table matching | Tabular rule matching |
| DmnDecisionTableNode | DMN decision table | Supports DMN specification |

### Interface Invocation Nodes

| Node Type | Description | Features |
| --------- | ----------- | -------- |
| HttpNode | HTTP request | Supports multiple timeout configurations, response condition judgment |
| FunctionNode | Custom expression | Aviator script execution |

### Message Queue Nodes

| Node Type | Description | Features |
| --------- | ----------- | -------- |
| RabbitMQNode | RabbitMQ message sending | Supports multiple exchange types |
| KafkaNode | Kafka message sending | Supports partitioning, message acknowledgment mode |
| MQTTNode | MQTT message publishing | Supports QoS configuration |

### File Operation Nodes

| Node Type | Description | Implementation |
| --------- | ----------- | -------------- |
| FTPNode | FTP file transfer | Apache Commons Net |
| DownloadFileNode | HTTP file download | - |
| SshNode | SSH remote command execution | JSch |
| ShellNode | Shell command execution | - |

### Communication Nodes

| Node Type | Description | Features |
| --------- | ----------- | -------- |
| EmailNode | Email sending | Supports HTML/plain text, attachments, SSL |

### AI Nodes

| Node Type | Description | Supported Platforms |
| --------- | ----------- | ------------------- |
| AITextNode | AI text generation | Deepseek, Volcengine |
| AIImageNode | AI image generation | Gemini, Volcengine |
| AITTSNode | AI text-to-speech | Gemini |

## 🛠️ Technology Stack

| Technology      | Version | Purpose                                      |
| --------------- | ------- | -------------------------------------------- |
| Java            | 21      | Main development language (LTS version)      |
| Spring Boot     | 3.5.7   | Service framework                            |
| Akka            | 2.6.20  | Actor model for high-concurrency scheduling  |
| Aviator         | 5.4.3   | High-performance expression engine           |
| Quartz          | -       | Scheduled task framework                     |
| Spring Data JPA | 3.5.7   | Persistence framework                        |
| MySQL           | 8.0     | Primary database                             |
| Redis           | -       | Cache support                                |
| RabbitMQ        | 5.20.0  | Message queue                                |
| Kafka           | 3.7.0   | Message queue                                |
| MQTT            | 1.2.5   | IoT messaging protocol                       |
| Hutool          | 5.7.18  | Java utility library                         |
| Lombok          | 1.18.42 | Simplify Java code                           |

## 🔧 Deployment Methods

- **Independent Deployment**: Deploy as an independent service, providing REST API interfaces for other systems to call
- **Embedded Deployment**: Can be embedded into other Java applications for use as part of the application
- **Cluster Deployment**: Supports multi-instance deployment, achieving high availability and horizontal scaling through load balancing

## 📋 Development Specifications

- **Code Style**: Follows Alibaba Java Development Specifications
- **Naming Conventions**: Uses camelCase naming, clearly expressing the meaning of variables and methods
- **Comment Specifications**: Adds detailed comments to key code for easy understanding and maintenance
- **Testing Specifications**: Writes unit tests and integration tests to ensure code quality

## 🎯 Application Scenarios

✅ **Applicable Scenarios (High Concurrency, Fast Decision Making, Complex Conditions)**

| Scenario | Core Features | Example |
| -------- | ------------- | ------- |
| Real-time Risk Assessment | Millisecond-level multi-dimensional parallel judgment | Credit Approval: Simultaneously checks credit score, work experience, debt ratio rules |
| Complex Cost Calculation | Multi-condition branches, complex calculation | Order Discount: Simultaneously calculates member discount, regional shipping, promotional activities |
| Dynamic Marketing Recommendations | Short-time triggered personalized actions | App Push: Judges login time, shopping cart status, pushes corresponding messages |
| Business Rule Management | Separation of rules from code | Insurance Claims: Automatically calculates based on type, amount, customer level |
| Compliance Checking | Multi-dimensional compliance checks | Financial Transactions: Anti-money laundering, anti-fraud, compliance checks |

❌ **Not Applicable Scenarios (Transactional, Manual Intervention)**

| Scenario | Core Features | Example |
| -------- | ------------- | ------- |
| Manual Approval Flow | Requires long wait for manual input | Employee Reimbursement: Waiting for financial review, leadership signature |

## 📸 Screenshot Display

### Rule Engine Interface

![Rule Engine Interface](screenshot/规则引擎0.png)
![Rule Engine Interface 2](screenshot/规则引擎1.png)
![Rule Engine Interface 3](screenshot/规则引擎2.png)
![Rule Engine Interface 4](screenshot/规则引擎3.png)
![Rule Engine Interface 5](screenshot/规则引擎4.png)
![Rule Engine Interface 6](screenshot/规则引擎5.png)
![Rule Engine Interface 7](screenshot/规则引擎6.png)
![Rule Engine Interface 8](screenshot/规则引擎7.png)

### Condition Construction Interface

![Condition Construction Interface](screenshot/条件构造.png)

### Expression Editing Interface

![Expression Editing Interface](screenshot/编辑表达式.png)

## 🚀 Quick Start

### Environment Requirements

- JDK 21+
- Maven 3.6+
- MySQL 8.0+

### Installation and Running

1. **Clone the project**

   ```bash
   git clone https://github.com/threefish/rule-engine.git
   cd rule-engine
   ```

2. **Configure the database**

   Modify the database configuration in `service/src/main/resources/application.yml`:

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://127.0.0.1:3306/rule
       username: root
       password: 123456
   ```

3. **Build the project**

   ```bash
   mvn clean package -DskipTests
   ```

4. **Start the service**

   ```bash
   java -jar service/target/service-0.0.1-SNAPSHOT.jar
   ```

5. **Access the frontend page**

   The console will output the frontend access address, click to access

### Core Configuration

```yaml
rule:
  akkaSystemName: xj-rule                    # Akka system name
  akkaDefaultExecuteTimeoutSeconds: 600      # Default execution timeout (seconds)
  akkaGlobalWorkerPoolInitSize: 100          # Worker pool initial size
  akkaGlobalWorkerPoolMaxSize: 2000          # Worker pool max size
  attachmentPath: /path/to/attachment        # Attachment storage path
```

### Usage Examples

**Embedded Mode: Execute Rule Flow**

```java
RuleFlowExcuteService ruleFlowExcuteService = ...; // Get service instance
Map<String, Object> variables = new HashMap<>(); // Set input variables
variables.put("age", 18);
variables.put("score", 85);

ExcuteRuleFlow request = new ExcuteRuleFlow();  
request.setKey("myRuleKey");  // Unique identifier of the rule flow
request.setVariables(variables);  // Input variables
request.setAsyncExcute(false);  // Synchronous execution

ExcuteRuleFlowResult result = ruleFlowExcuteService.startFlow(request);

// Process execution results
if (result.isSuccess()) {
    System.out.println("Rule execution successful!");
    System.out.println("Execution result: " + result.getResponse());
} else {
    System.out.println("Rule execution failed: " + result.getErrorMessage());
}
```

**Independent Deployment Mode: Execute Rule Flow**

```shell
curl -X POST \
-H "Authorization: YOUR_TOKEN" \
-H "Content-Type: application/json" \
-d '{"key": "myRuleKey", "requestId": "1111111", "variables": {"key1": "value1", "key2": "value2"}}' \
http://host:port/openapi/v1/ruleflow/excute
```

## 🤝 Contribution Guide

Welcome to participate in the contribution and jointly improve the rule engine project!

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project uses the Apache 2.0 license, see the [LICENSE](LICENSE) file for details.

## 🔮 Future Planning

- [ ] Support more node types
- [ ] Support distributed deployment and horizontal scaling
- [ ] Enhance real-time monitoring and alerting functions for rule execution
- [ ] Provide visualized analysis tools for rule execution

## 📞 Contact Information

**Author**: Huang Chuan (huchuc@vip.qq.com)

**GitHub**: https://github.com/threefish/rule-engine

**Frontend Project**: https://github.com/threefish/rule-engine-web

---

**If you find this project helpful, please give it a Star ⭐ support!**

---

*Continuously updating...*
