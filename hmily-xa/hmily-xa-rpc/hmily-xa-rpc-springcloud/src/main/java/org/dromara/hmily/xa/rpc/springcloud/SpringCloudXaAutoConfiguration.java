package org.dromara.hmily.xa.rpc.springcloud;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(FeignClient.class)
public class SpringCloudXaAutoConfiguration {

    @Bean
    public RequestInterceptor hmilyXaInterceptor() {
        return new FeignRequestInterceptor ();
    }

    @Bean
    public FeignBeanPostProcessor hmilyXaPostProcessor() {
        return new FeignBeanPostProcessor ();
    }

}
