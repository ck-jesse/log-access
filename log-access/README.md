# log-access

## 介绍
将接口方法的访问日志（入参和出参）输出到access日志文件；
支持的方法类型：http请求方法、dubbo请求方法、普通方法。
注：可结合ELK(Elasticsearch,Logstash,Kibana)通用日志解决方案来可视化日志。

# 一、log-access功能描述

1、支持 http请求、dubbo请求、普通方法 的访问日志（入参和出参）输出到日志文件；

2、支持 [字符串] 最大输出长度限制，如：字段长度为500，仅输出前256个字符，其他的省略；

3、支持 [集合] 最大输出元素限制，如：集合大小为10，仅输出集合中的前3个元素，其他的省略；

4、支持将敏感数据脱敏后再输出到日志文件（目前支持手机号和身份证号的脱敏）；

5、支持跨服务链路追踪（基于trace_id实现）


# 二、使用log-access的配置步骤：

## 1、Maven包引入
````xml
<dependency>
   <groupId>com.tn.log</groupId>
   <artifactId>log-access</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>
````
注意：要特别注意log-access组件中依赖的spring和dubbo相关的包可能与组件使用方中的版本有冲突，所以需要解决冲突后才能使用。

方案一：将log-access组件中的spring或dubbo给排除掉（建议使用）

方案二：将组件使用方中的spring或dubbo给排除掉
````xml
<dependency>
   <groupId>com.tn.log</groupId>
   <artifactId>log-access</artifactId>
   <version>1.0-SNAPSHOT</version>
   <!-- 排除依赖包 -->
   <exclusions>
       <exclusion>
           <artifactId>spring-web</artifactId>
           <groupId>org.springframework</groupId>
       </exclusion>
   </exclusions>
</dependency>
````

## 2、引入spring配置



### 方式一： 在组件使用方的spring配置文件中引入如下配置

````xml
<import resource="classpath*:META-INF/spring/log-access.xml"/>
````
### 方式二： 基于注解 @EnableLogAccess 的方式（结合SpringBoot使用）

````java
@EnableLogAccess
public class LogAccessConfig {
    
}
````
注：上面两种方式二选一即可



## 3、配置logback.xml配置（此处已logback为例）

````xml
<!-- 注意：不同的日志框架按照各自的配置方式来配置即可，主要是注意<logger name="access_logger"/>元素的配置，其中name必须为access_logger -->
<!-- 定义 access 访问日志输出 -->
<appender name="ACCESSLOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
   <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
       <level>INFO</level>
   </filter>
   <file>${LOG_DIR}/${APP_NAME}-access.log</file>
   <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
       <fileNamePattern>${LOG_DIR}/${APP_NAME}-access-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
       <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
           <maxFileSize>500MB</maxFileSize>
       </timeBasedFileNamingAndTriggeringPolicy>
   </rollingPolicy>
   <encoder>
       <pattern>%m%n</pattern>
       <charset>UTF-8</charset>
   </encoder>
</appender>
<!-- access performance logger -->
<logger name="access_logger" level="INFO" additivity="false">
   <appender-ref ref="ACCESSLOG"/>
</logger>
````

## 4、代码中的使用

注：在需要打印出入参数的类或者方法上标记@LogAccess注解

````java
// 方式一：@LogAccess标记在类上，对类的所有方法起作用
@LogAccess
public class MessageFacadeImpl implements MessageFacade {
    @Override
    public Result<MessageDTO> sendMessage(String msgTye, String msg) {
        // xxxx
    }
}
````
````java
// 方式二：@LogAccess标记在方法上，仅对该方法起作用
public class MessageFacadeImpl implements MessageFacade {
    @LogAccess
    @Override
    public Result<MessageDTO> sendMessage(String msgTye, String msg) {
        // xxxx
    }
}
````

## 5、访问日志的格式
````json
【格式】
访问时间|执行耗时ms|执行结果|请求类型|模块名|接口名|本机IP|客户端IP|消息跟踪号|请求参数|响应参数|attachment|附件参数

【字段描述】
访问时间：开始的访问时间，
执行结果：用于区分该请求是否正常响应，N 对应normal，表示正常响应，E 对应error，表示出现异常，
请求类型：METHOD 表示普通方法、HTTP 表示http请求、DUBBO 表示dubbo请求，
执行耗时：业务方法的执行时间，单位毫秒(ms)，
消息跟踪号：对应trace_id，用于日志链路追踪
请求参数：json格式串
响应参数：json格式串
attachment：用于标记后面的是附件参数
附件参数：以便灵活扩展（如cookie和header中的内容等）

注意：
1、对于入参和出参对象中的集合只输出指定数量的元素，如：集合大小为10，仅输出集合中的前3个元素，其他的省略
2、对于入参和出参对象中的字符串字段控制最大的输出长度，如：字段长度为500，仅输出前256个字符，其他的省略
````


## 6、访问日志样例

### 5.1 普通方法的访问日志样例
```json
2019-05-14 16:32:07.411|N|METHOD|43ms|com.xx.xx.XXService|xxMethod|10.1.1.48|10.1.1.48|3a1979e559e24c56b8b866731155c889|["1000008684","fast"]|{"status":200,"message":null,"data":{"code":"success","createTime":1557492448000,"updateTime":1557492979000},"error":false,"success":true}|attachtment|
```
### 5.2 http接口的访问日志样例
```json
2019-05-14 16:32:07.405|N|HTTP|104ms|xx-web|test/test|10.1.1.48|127.0.0.1|3a1979e559e24c56b8b866731155c889|fast|{"status":200,"message":null,"data":{"code":"success","createTime":1557492448000,"updateTime":1557492979000},"error":false,"success":true}|attachtment|
```
### 5.3 dubbo接口的访问日志样例
```json
2019-05-14 13:54:42.880|N|DUBBO|57ms|dubbo://127.0.0.1:20885/group/com.xx.xx.XXFacade|queryXX|10.1.1.48|10.1.1.48:50542|3a1979e559e24c56b8b866731155c889|["1000008684",3,"fast"]|{"status":200,"message":null,"data":{"code":"success","createTime":1557492448000,"updateTime":1557492979000},"error":false,"success":true}|attachtment|
```


## 7、跨服务链路追踪

### 7.1 rest服务间的链路追踪
> 原理：通过自定义ServletTraceInfoAttachmentFilter，从request的header中获取trace_id，实现本服务的日志链路追踪。
>
> 注：若需跨服务将trace_id传递到服务方，那么需要根据不同的调用方式来进行扩展。
>
> 1、若基于RestTemplate，则扩展ClientHttpRequestInterceptor将trace_id设置到header中传递到服务提供方。
>
> 2、若基于Feign，则扩展RequestInterceptor将trace_id设置到header中传递到服务提供方。
>
```java
@EnableLogAccess
public class LogAccessConfig {
    /**
     * 配置http请求日志跟踪信息拦截器，header中无trace_id则生成
     */
    @Bean
    public FilterRegistrationBean logFilterRegister() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ServletTraceInfoAttachmentFilter());
        registration.setName(ServletTraceInfoAttachmentFilter.class.getSimpleName());
        registration.addUrlPatterns("/*");
        // 从小到大的顺序来依次过滤
        registration.setOrder(1);
        return registration;
    }

    /**
     * 基于RestTemplate的服务调用
     */
    @Bean("restTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(httpRequestFactory());
        // 将trace_id设置到header中传递到服务提供方
        restTemplate.setInterceptors(Arrays.asList(new ClientHttpRequestInterceptor(){
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                String traceId = MDCLogTracerContextUtil.getTraceId();
                if (traceId != null) {
                    request.getHeaders().add(TracingVariable.TRACE_ID, traceId);
                }
                return execution.execute(request, body);
            }
        }));
        return restTemplate;
    }

    /**
     * 基于Feign的服务调用
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        RequestInterceptor requestInterceptor = template -> {
            String traceId = MDCLogTracerContextUtil.getTraceId();
            if (traceId != null) {
                template.header(TracingVariable.TRACE_ID, traceId);
            }
        };
        return requestInterceptor;
    }
}
```


### 7.2 dubbo服务间的链路追踪
> 原理：dubbo提供了良好的spi扩展机制，通过扩展Filter，实现服务间的trace_id传递
> 1、通过自定义DubboTraceInfoDetachmentFilter，从RpcContext的attachment属性中获取trace_id
>
> 2、通过自定义DubboTraceInfoAttachmentFilter，将trace_id设置到RpcContext的attachment属性中，透传到服务提供方
>
>

### 7.3 跨线程池的链路追踪
> 原理：通过对Runnable或Thread进行装饰，将主线程中ThreadLocal的内容设置到子线程的ThreadLocal即可
> 1、
```java


Map<String, String> contextMap = MDC.getCopyOfContextMap();
// 对Runnable进行装饰，将主线程的MDC内容设置到子线程的MDC中
Runnable runnable = new Runnable() {
    @Override
    public void run() {
        try {
            MDC.setContextMap(contextMap);
            runnable.run();
        } finally {
            MDC.clear();
        }
    }
};

```