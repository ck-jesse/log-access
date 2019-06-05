使用log-access的配置步骤：

1、Maven包引入
 <dependency>
    <groupId>com.tn.log</groupId>
    <artifactId>log-access</artifactId>
    <version>xxx</version>
 </dependency>
 注意：要特别注意spring和dubbo相关包与要引入log-access组件的服务中的spring和dubbo的版本是否有冲突。

2、引入spring配置文件
 <import resource="classpath*:META-INF/spring/log-access.xml"/>
 注：后续再支持基于注解的方式来进行启动配置

3、配置logback.xml配置（此处已logback为例）
 注意：不同的日志框架按照各自的配置方式来配置即可，主要是注意<logger name="access_logger"></>元素的配置，其中name必须为access_logger

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

4、代码中的使用
 在需要打印出入参数的类或者方法上标记@LogAccess注解