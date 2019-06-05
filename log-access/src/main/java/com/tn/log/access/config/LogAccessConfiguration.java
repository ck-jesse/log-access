package com.tn.log.access.config;

import com.tn.log.access.aspect.LogAccessAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置启动扫描LogAccess组件下的包的
 * 方式一：
 * 使用@ComponentScan(basePackages = {"com.tn.log.access"})配置扫描包
 * 方式二：
 * 使用@Bean直接定义LogAccessAspect 作为spring容器中的bean
 *
 * @author chenck
 * @date 2019/5/14 14:35
 */
@Configuration
public class LogAccessConfiguration {

    private int order = -1000;

    /**
     * 定义LogAccessAspect bean
     */
    @Bean
    public LogAccessAspect logAccessAspect() {
        LogAccessAspect logAccessAspect = new LogAccessAspect();
        logAccessAspect.setOrder(order);
        return logAccessAspect;
    }
}
