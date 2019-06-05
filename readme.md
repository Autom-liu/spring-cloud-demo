# spring-cloud 核心功能构建

# spring cloud 基本项目依赖

父工程项目pom.xml

```xml
<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.0.1.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<spring-cloud.version>Finchley.SR1</spring-cloud.version>
		<mapper.starter.version>2.0.3</mapper.starter.version>
		<mysql.version>5.1.32</mysql.version>
		<pageHelper.starter.version>1.2.5</pageHelper.starter.version>
	</properties>
	
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<dependency>
				<groupId>tk.mybatis</groupId>
				<artifactId>mapper-spring-boot-starter</artifactId>
				<version>${mapper.starter.version}</version>
			</dependency>
			<dependency>
				<groupId>com.github.pagehelper</groupId>
				<artifactId>pagehelper-spring-boot-starter</artifactId>
				<version>${pageHelper.starter.version}</version>
			</dependency>
			<dependency>
				<groupId>mysql</groupId>
				<artifactId>mysql-connector-java</artifactId>
				<version>${mysql.version}</version>
			</dependency>
		</dependencies>
	</dependencyManagement>
	
	<dependencies>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
```

# Eureka注册中心

## 添加Eureka注册中心服务

### 添加依赖——eureka服务端

创建新的子工程，添加如下依赖：

pom.xml:

```xml
<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

### 增加启动类注解

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServer {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(EurekaServer.class, args);
	}
}
```

### 配置application.yml文件

```
server:
  port: 10086
spring:
  application:
    name: eureka-server
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
  instance:
    prefer-ip-address: true		# 固定ip访问，目前还没看到生效
    ip-address: 127.0.0.1 		# 固定ip访问，目前还没看到生效
```

### 注册方添加erueka客户端依赖

Erueka作为注册中心，其他往它注册的服务都是客户端，无论它是提供方还是消费方

pom.xml

```xml
<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 注册方启动类添加注解

注册方启动类需要添加服务发现注解

> @EnableDiscoveryClient

### 调用服务写法一

调用服务的其中一种写法，就是通过`DiscoveryClient`类手动从注册中心获取实例列表
拿到实例，拼接URL，发起请求：

```java
@RestController
@RequestMapping("consumer")
public class ConsumerController {
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private DiscoveryClient discoverClient;
	
	@GetMapping("/{id}")
	public User queryById(@PathVariable("id") String id) {
		// 根据服务id获取实例列表
		List<ServiceInstance> instances = discoverClient.getInstances("user-service");
		// 从实例列表中拿到实例
		ServiceInstance instance = instances.get(0);
		// 构造URL
		String url = "http://"+ instance.getHost() +":"+ instance.getPort() +"/user/" + id;
		// 发起请求
		User user = restTemplate.getForObject(url, User.class);
		return user;
	}
	
}
```

这种方式感到很low有没有？

### 注册方配置erueka注册中心地址

需要注意的是一定要给每个服务命名，即`spring.application.name`

```
spring:
  application:
    name: user-consumer
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
  instance:
    prefer-ip-address: true
    ip-address: 127.0.0.1 
```

## erueka 高级配置

### 高可用集群

> eureka.client.service-url.defaultZone 值配置多个即可

### 服务注册

其实就是配置是否自己作为服务注册，默认注册自己，可以配置不注册自己

> eureka.register-with-eureka: false

### 服务续约

注册中心和注册方维持的心跳机制，几秒告知一次健康状态

> eureka.instance.lease-renewal-interval-in-seconds

开发环境小一点，生产环境默认即可，不宜太小也不宜太大

### 服务失效

当注册方迟迟未心跳时，配置几秒认为它不存活了

> eureka.instance.lease-expiration-duration-in-seconds

### 服务刷新

默认情况下会缓存服务列表的数据，隔一定时间重新拉取更新，通过该参数可以修改拉取时间，在开发环境下通常设置小一点，生产环境下不需要更改  单位秒

> eureka.client.registry-fetch-interval-seconds

### 自我保护

spring cloud注重CAP原则中的A和P，简单来说即不轻易相信服务挂了，即它会排查一切可能，只有确定100%认为它挂了它才剔除，否则只要有存活的可能，都不会放弃，哪怕它真的挂了...

开启自我保护，当服务没有正常保持心跳时，注册中心并不认为它挂了，因为存在网络延迟的可能，因此要统计次数及比例，只有超过50%（或多少的~）才可以认为它真的挂了。

这样能有效防止注册中心误判（服务挂没挂）

一般不需要关闭，非要关闭就进行如下配置为`false`即可：

> eureka.server.enable-self-preservation

### 失效剔除

前面说到，注册中心不会轻易判定一个服务说挂就挂的，因此哪怕注册中心真的认为服务挂了，也要缓冲一个时间，给它最后一次机会，实在不给面子了，才会把它剔除。

因此失效剔除也是需要有时间的，配置如下：单位毫秒

> eureka.server.eviction-interval-timer-in-ms

# Ribbon 负载均衡器

## 引入相关依赖

在服务消费方引用

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
</dependency>
```

## 使用RibbonLoadBalancerClient动态获取实例

```java
@RestController
@RequestMapping("consumer")
public class ConsumerController {
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RibbonLoadBalancerClient ribbonClient;
	
	@GetMapping("/{id}")
	public User queryById(@PathVariable("id") String id) {
		// 使用ribbon负载均衡器可以直接id实例
		ServiceInstance instance = ribbonClient.choose("user-service");
		String url = "http://"+ instance.getHost() +":"+ instance.getPort() +"/user/" + id;
		User user = restTemplate.getForObject(url, User.class);
		return user;
	}
	
}
```

## 调用服务写法二————使用Ribbon拦截器拦截RestTemplate

第二种写法，即使用Ribbon拦截器拦截RestTemplate，它对RestTemplate做了一次代理，省去了拼接URL麻烦，直接调用访问即可

首先要将注册到spring中的restTemplate添加注解标记

```java
@Bean
@LoadBalanced   // 使用该注解，会对restTemplate所有请求进行一个拦截处理url
public RestTemplate restTemplate() {
	return new RestTemplate();
}
```

这样就可以不再通过RibbonLoadBalancerClient去获取，而是直接体现在URL上，在RestTemplate发起请求之前重写URL

```java
@RestController
@RequestMapping("consumer")
public class ConsumerController {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/{id}")
	public User queryById(@PathVariable("id") String id) {
		// 将服务id直接写进url上
		String url = "http://user-service/user/" + id;
		User user = restTemplate.getForObject(url, User.class);
		return user;
	}
	
}
```

## Ribbon 高级配置

### 负载均衡策略

Ribbon默认负载均衡策略是轮询的，可以通过如下配置修改负载均衡策略

> [service-id].ribbon.NFLoadBalancerRuleClassName

其中`service-id`是服务提供方的应用名称，即`spring.application.name`配置的名称

负载均衡策略都在`com.netflix.loadbalancer`包下：

其中有如下几种策略：

- RoundRobinRule
- AvailabilityFilteringRule
- WeightedResponseTimeRule
- ZoneAvoidanceRule
- BestAvailableRule
- RandomRule
- Retry

也可以自定义策略，这个上网去查了

### 重试机制

ribbon重试机制个人感觉有问题，没成功过，或偶尔成功过

某些人说要加入如下依赖：

```xml
<dependency>
		<groupId>org.springframework.retry</groupId>
		<artifactId>spring-retry</artifactId>
</dependency>
```

但另外一些人又说那是老版本的做法，这里用的是新版本的，不知道会不会问题。

但大多数主要就做如下配置即可的：

> [service-id].ribbon.ConnectTimeOut  Ribbon连接超时时间

> [service-id].ribbon.ReadTimeout  Ribbon读取数据超时时间

> [service-id].ribbon.OkToRetryOnAllOperations  是否对所有操作都进行重试

> [service-id].ribbon.MaxAutoRetriesNextServer  切换实例的重试次数

> [service-id].ribbon.MaxAutoRetries  对当前实例的重试次数

但请务必先开启spring cloud重试功能：

> spring.cloud.loadbalancer.retry.enabled: true
