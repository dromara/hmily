package org.dromara.hmily.xa.rpc.springcloud;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudXaAutoConfiguration {

    @Bean
    public RequestInterceptor hmilyXaInterceptor() {
        return new FeignRequestInterceptor ();
    }

    @Bean
    public FeignBeanPostProcessor hmilyXaPostProcessor() {
        return new FeignBeanPostProcessor ();
    }


//    @Configuration
//    @ConditionalOnClass(ILoadBalancer.class)
//    static class LoadBalancerConfig {
//
//    }
}
