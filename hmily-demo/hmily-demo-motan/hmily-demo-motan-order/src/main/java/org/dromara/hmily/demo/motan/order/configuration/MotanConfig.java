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
public class MotanConfig
{
    /**
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "hmily.motan.annotation")
    public AnnotationBean motanAnnotationBean() {
        AnnotationBean motanAnnotationBean = new AnnotationBean();
        return motanAnnotationBean;
    }

    /**
     *
     * @return
     */
    @Bean(name = "hmilyMotan")
    @ConfigurationProperties(prefix = "hmily.motan.protocol")
    public ProtocolConfigBean protocolConfig() {
        ProtocolConfigBean config = new ProtocolConfigBean();
        return config;
    }

    /**
     *
     * @return
     */
    @Bean(name = "hmilyRegistryConfig")
    @ConfigurationProperties(prefix = "hmily.motan.registry")
    public RegistryConfigBean registryConfig() {
        RegistryConfigBean config = new RegistryConfigBean();
        return config;
    }
}
