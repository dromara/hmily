package org.dromara.hmily.demo.motan.order.configuration;

import com.weibo.api.motan.config.springsupport.BasicServiceConfigBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The MotanServerConfig.
 *
 * @author bbaiggey
 */
@Configuration
public class MotanServerConfig
{
    /**
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "hmily.motan.server")
    public BasicServiceConfigBean baseServiceConfig() {
        BasicServiceConfigBean config = new BasicServiceConfigBean();
        return config;
    }
}
