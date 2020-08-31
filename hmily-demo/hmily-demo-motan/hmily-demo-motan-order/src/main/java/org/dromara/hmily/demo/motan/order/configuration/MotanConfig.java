package org.dromara.hmily.demo.motan.order.configuration;

import com.weibo.api.motan.config.springsupport.AnnotationBean;
import com.weibo.api.motan.config.springsupport.ProtocolConfigBean;
import com.weibo.api.motan.config.springsupport.RegistryConfigBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The MotanConfig.
 *
 * @author bbaiggey
 */
@Configuration
public class MotanConfig {
    
    /**
     * Motan annotation bean annotation bean.
     *
     * @return the annotation bean
     */
    @Bean
    @ConfigurationProperties(prefix = "hmily.motan.annotation")
    public AnnotationBean motanAnnotationBean() {
        return new AnnotationBean();
    }
    
    
    /**
     * Protocol config protocol config bean.
     *
     * @return the protocol config bean
     */
    @Bean(name = "hmilyMotan")
    @ConfigurationProperties(prefix = "hmily.motan.protocol")
    public ProtocolConfigBean protocolConfig() {
        return new ProtocolConfigBean();
    }
    
    /**
     * Registry config registry config bean.
     *
     * @return the registry config bean
     */
    @Bean(name = "hmilyRegistryConfig")
    @ConfigurationProperties(prefix = "hmily.motan.registry")
    public RegistryConfigBean registryConfig() {
        return new RegistryConfigBean();
    }
}
