# PaySphere Gateway

PaySphere Gateway 是一个基于 Spring Cloud Gateway 的微服务网关项目，提供路由转发、负载均衡等功能。

## 环境要求

- JDK 17+
- Maven 3.8+
- Spring Boot 3.0.0
- Spring Cloud 2022.0.0

## 项目结构

```
paysphere-gateway
├── src/main/java
│   └── com/paysphere/gateway
│       ├── config          # 配置类
│       ├── filter          # 过滤器
│       ├── handler         # 处理器
│       └── util            # 工具类
└── src/main/resources
    ├── application.yml     # 主配置文件
    ├── application-dev.yml # 开发环境配置
    ├── application-test.yml# 测试环境配置
    ├── application-prod.yml# 生产环境配置
    └── logback-spring.xml  # 日志配置
```

## 主要功能

- 路由转发
- 负载均衡
- 日志追踪
- 监控指标
- 缓存支持
- 消息队列集成

## 快速开始

1. 克隆项目
```bash
git clone [项目地址]
```

2. 编译打包
```bash
mvn clean package
```

3. 运行项目
```bash
# 开发环境
java -jar target/paysphere-gateway.jar --spring.profiles.active=dev

# 测试环境
java -jar target/paysphere-gateway.jar --spring.profiles.active=test

# 生产环境
java -jar target/paysphere-gateway.jar --spring.profiles.active=prod
```

## 配置说明

### 基础配置

```yaml
spring:
  application:
    name: paysphere-gateway
  profiles:
    active: dev
  cloud:
    gateway:
      httpclient:
        pool:
          maxIdleTime: 55000
```

### 环境配置

项目支持多环境配置：
- 开发环境 (dev)
- 测试环境 (test)
- 生产环境 (prod)

### 日志配置

日志配置位于 `logback-spring.xml`，支持：
- 控制台输出
- 文件输出（按天滚动）
- 错误日志分离
- 异步日志处理

## 依赖说明

主要依赖：
- Spring Cloud Gateway
- Spring Cloud LoadBalancer
- Spring Boot Actuator
- TLog
- Hutool
- Guava
- RocketMQ
- Caffeine

## 监控指标

项目集成了 Spring Boot Actuator，提供以下监控端点：
- 健康检查：`/actuator/health`
- 指标信息：`/actuator/metrics`
- 环境信息：`/actuator/env`

## 开发指南

1. 添加新路由
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-route
          uri: lb://service-name
          predicates:
            - Path=/api/**
```

2. 添加自定义过滤器
```java
@Component
public class CustomFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 实现过滤逻辑
        return chain.filter(exchange);
    }
}
```

## 部署说明

1. 生产环境部署建议：
   - 使用 Docker 容器化部署
   - 配置合适的 JVM 参数
   - 启用监控告警
   - 配置日志收集

2. JVM 参数建议：
```bash
java -Xms2g -Xmx2g -jar paysphere-gateway.jar
```

## 常见问题

1. 网关无法启动
   - 检查端口是否被占用
   - 检查配置文件是否正确
   - 检查日志输出

2. 路由转发失败
   - 检查目标服务是否可用
   - 检查路由配置是否正确
   - 检查网络连接

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交代码
4. 创建 Pull Request

## 版本历史

- v1.0.0
  - 初始版本
  - 基础网关功能
  - 多环境支持
  - 日志追踪

## 许可证

[许可证类型]

## 联系方式

[联系信息] 
