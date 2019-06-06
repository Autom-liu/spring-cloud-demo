
[TOC]

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

以下这些配置均是在`AbstractRibbonCommand`类源码中有对应指明

> [service-id].ribbon.ConnectTimeOut  Ribbon连接超时时间

> [service-id].ribbon.ReadTimeout  Ribbon读取数据超时时间

> [service-id].ribbon.OkToRetryOnAllOperations  是否对所有操作都进行重试

> [service-id].ribbon.MaxAutoRetriesNextServer  切换实例的重试次数

> [service-id].ribbon.MaxAutoRetries  对当前实例的重试次数

但请务必先开启spring cloud重试功能：

> spring.cloud.loadbalancer.retry.enabled: true

# Hystrix 降级熔断

所谓熔断，即服务超出一定时长未响应及时反馈用户，避免因为阻塞占用连接。

## 添加Hytrix依赖

pom.xml

```xml
	<dependency>
	    <groupId>org.springframework.cloud</groupId>
	    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
	</dependency>
```

## 启动类开启Hystrix注解

### @EnableHystrix

这个注解仅仅只是Hystrix的基本功能，并不通用

### @EnableCircuitBreaker

这个注解包含了服务熔断，服务降级等处理，更为通用

### @SpringCloudApplication

这个注解其实是`@SpringBootApplication`，`@EnableDiscoveryClient`，`@EnableCircuitBreaker`，三者合一，是spring cloud项目开发的基本注解！！

## 设置接口熔断回调

### 局部熔断回调

使用`@HystrixCommand`的`fallbackMethod`属性配置，具体看如下代码：

```java
@RestController
@RequestMapping("consumer")
public class ConsumerController {
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private LoadBalancerClient ribbonClient;
	
	@GetMapping("/{id}")
	@HystrixCommand(fallbackMethod = "queryFailback")
	public Result<User> queryById(@PathVariable("id") String id) {
		ServiceInstance instance = ribbonClient.choose("user-service");
		String host = instance.getHost();
		int port = instance.getPort();
		
		String url = String.format("http://%s:%d/user/%s", host, port, id);
		System.out.println(url);
		User user = restTemplate.getForObject(url, User.class);
		return Result.success(user);
	}
	
	public Result<User> queryFailback(String id) {
		return Result.error("服务器繁忙，请稍后再试....");
	}
	
}
```

### 公共熔断回调

所谓公共熔断回调即对整个类生效，只需要在类上配置注解`@DefaultProperties`的`defaultFallback`属性，在需要熔断的方法上加上`@HystrixCommand`即可

参考代码如下：

```java
@RestController
@RequestMapping("consumer")
@DefaultProperties(defaultFallback = "defaultFailback")
public class ConsumerController {
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private LoadBalancerClient ribbonClient;
	
	@GetMapping("/{id}")
	@HystrixCommand
	public Result<User> queryById(@PathVariable("id") String id) {
		ServiceInstance instance = ribbonClient.choose("user-service");
		String host = instance.getHost();
		int port = instance.getPort();
		
		String url = String.format("http://%s:%d/user/%s", host, port, id);
		System.out.println(url);
		User user = restTemplate.getForObject(url, User.class);
		return Result.success(user);
	}
	
	public Result<User> defaultFailback() {
		return Result.error("服务器繁忙，请稍后再试试....");
	}
	
}
```

## 高级配置

以下这些配置其实都在`HystrixCommandProperties`类源码中有对应指出的，并不是乱来的。

### 全局超时时间

> hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds

### 局部超时时间

接口方法上加上如下注解：

```java
@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "6000")
	})
```

### 熔断器请求量

熔断器会在一定的请求次数后对请求进行分析统计，并跳入下一个状态，比如统计失败次数过多，将进入熔断状态。

通过如下配置项进行配置：

> circuitBreaker.requestVolumeThreshold

### 出错比例

前面说到达到一定的请求进行一次统计，比如统计失败次数过多，将进入熔断状态，这里的过多，其实是根据一定比例来判定的，即出错比例

可以通过如下配置项进行配置：

> circuitBreaker.errorThresholdPercentage

### 休眠时间窗

熔断器熔断后并不是完事了，还要尝试恢复，因此会在一定的时间后从熔断状态变为半开放状态，测试当前服务集群可用性，这个时间即休眠时间窗

可用通过如下配置项进行配置：

> circuitBreaker.sleepWindowInMilliseconds

通过注解的方式配置，结合**局部超时时间**、**熔断请求量**、**休眠时间窗**、**出错比例**一起使用，最终配置的结果如下：

```java
@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "6000"),
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),
	})
```

# feign

## 基本使用

feign基本功能即代替restTemplate写法，注意它只是远程调用的工具，封装以简化代码开发而已的，并没有改变远程调用的工作。

### 导入依赖

feign 已经包含了ribbon和hystrix依赖，但是为了全面，将同时导入也无所谓

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### 启动类添加注解

启动类添加如下注解开启feign客户端

> @EnableFeignClients

### 添加远程调用的服务消费端端接口

feign 远程调用其实是通过配置熟悉的`spring mvc`语法来构造URL，代替restTemplate方式的调用。

方式就是以接口的形式定义相关配置，如URI，返回值，参数列表等。这个接口方法的定义保持和`spring mvc`端的`web`层方法一致。

```java
@FeignClient("user-service")
@RequestMapping("/user")
public interface UserClient {

 	@GetMapping("/{id}")
	User queryById(@PathVariable("id") String id);
}
```

### 远程调用方式三————面向接口调用

前面说到两种远程调用的方式，都是需要显式指出URL，调用restTemplate，这样写看似代码逻辑清晰，简单明了。但是大量写死的代码造成代码冗余，逻辑重复，可维护性差。因此就出现了最终的高端写法：

面向接口调用，是一个相当抽象的过程，看不出来内部使用什么实现的。方式也很简单，不需要RestTempalte，也不需要LoadBalancerClient，直接注入前面自定义的配置接口即可，具体参考如下代码：

```java
@RestController
@RequestMapping("consumer")
public class ConsumerController {
	
	@Autowired
	private UserClient userClient;
	
	@GetMapping("/{id}")
	public Result<User> queryById(@PathVariable("id") String id) {
		User user = userClient.queryById(id);
		return Result.success(user);
	}
	
}
```

让你完全看不出远程调用的痕迹，这只是一种写法而已，底层仍然时想注册中心发起远程调用的。

这种写法说好听点就是减少代码冗余，避免代码重复，但其实就是装逼式写法，增加了理解难度，看得懂的人秀，看不懂的人怎么也别想不懂...  ^_^

## feign 整合 Ribbon 和 Hystrix 重试和熔断

> 温馨提示：不建议使用，因为没有成功的。__可能是我技术问题__

feign 本身提供了 Ribbon 和 Hystrix，它就相当于是spring cloud家族的一个集成工具，因此导入依赖的时候不需要`ribbon`的相关依赖（`Hystrix`还是要的，因为刚出版，没有完全集成）

### ribbon 和 Hystrix相关配置

大多数和原来一样的，只不过要开启feign

```pro
spring:
  application:
    name: user-consumer
  cloud:
    loadbalancer:
      retry:
        enabled: true   # 开启spring cloud 重试功能
user-service:
  ribbon:
    # NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule    # 更换负载均衡策略
    ConnectTimeout: 250     #  Ribbon连接超时时间
    ReadTimeout: 1000       #  Ribbon读取数据超时时间
    OkToRetryOnAllOperations: true    # 是否对所有操作都进行重试
    MaxAutoRetriesNextServer: 100      # 切换实例的重试次数
    MaxAutoRetries: 1                 # 对当前实例的重试次数
feign:
  hystrix:
    enabled: true           # 开启feign的hystrix熔断
ribbon:
  ConnectTimeout: 500    # 连接超时时长
  ReadTimeout: 1000         # 读取超时时长
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 6000    # 设置hystrix的超时时间
```

### 新建failback工厂作为熔断回调

建议用工厂模式，不然不容易成功

```java
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

 	@Override
	public UserClient create(Throwable cause) {
		return new UserClient() {

 			@Override
			public User queryById(String id) {
				return null;
			}
		};
	}

}
```

### 添加远程调用的服务消费端端接口

和之前一样，使用接口，但是需要在@FeignClient注解上添加多一个fallbackFactory 属性：

> @FeignClient(value = "user-service", fallbackFactory = UserClientFallbackFactory.class)

注意，feign的熔断不走hystrix的流程，因此feign配置和hystrix配置是会冲突的，因此如果要用`feign`的熔断功能的话，就要把原来的hystrix熔断相关的注解去掉，不然不会成功。（虽然就没成功过！）

# Zuul网关

是主要的对外接口，外部必须通过网关才能访问具体的微服务，网关同时具有服务聚合的功能。

简单来说，就是Controller

## 基本使用

网关作为单独的项目存在，也是`spring boot`服务

### 添加依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 启动类开启Zuul网关注解

> @EnableZuulProxy

注意使用ZuulProxy而不是ZuulApplication，功能更完善

## 高级配置

### 基于服务的路由配置

所谓路由配置即URI到服务的映射关系配置

这其实是Zuul的默认配置，默认它会将所有的服务名称作为前缀到URI上去，因此即使不配置，也可照常访问。

但是有时候需求，并不要求服务名称作为URI前缀访问时，就需要指定配置了。

```
zuul:
  routes:
    user-service:
      path: /user-service/**
      serviceId: user-service
```

### 忽略服务

有时候并不希望有些服务对外使用，但是网关默认是全部注册上的，因此需要配置忽略的服务

```
zuul:
  routes:
    user-service: /user/**
  ignored-services:
    - user-consumer
```

### 局部前缀忽略

访问 zuul.routes.[service-id].path 是需要带前缀的

```
zuul:
  routes:
    user-service:
      path: /user/**
      serviceId: user-service
      strip-prefix: false
```

### 全局访问前缀

> zuul.prefix: /api

### 整合Ribbon 和 Hystrix

配置其实和之前一个样，只需要加上如下配置即可

> zuul.retryable

```
zuul:
  retryable: true   # 开启重试
ribbon:
  ConnectTimeout: 1000
  ReadTimeout: 1000
  MaxAutoRetries: 0
  MaxAutoRetriesNextServer: 100
  OkToRetryOnAllOperations: true
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 6000    # 设置hystrix的超时时间
```

## 自定义过滤器

先直接看演示

```java
@Component
public class LoginFilter extends ZuulFilter {

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();
		
		String token = request.getParameter("token");
		
		if(StringUtils.isBlank(token)) {
			// 不存在，则拦截
			context.setSendZuulResponse(false);
			HttpServletResponse response = context.getResponse();
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType("text/json;charset=UTF-8");
			context.setResponse(response);
//			context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
			
			context.setResponseBody(JSON.toJSONString(Result.error("没有权限")));
		}
		
		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
	}

}
```
