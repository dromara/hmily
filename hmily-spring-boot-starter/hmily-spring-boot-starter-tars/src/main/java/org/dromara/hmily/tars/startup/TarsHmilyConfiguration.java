package org.dromara.hmily.tars.startup;

import com.qq.tars.client.Communicator;
import org.dromara.hmily.tars.spring.TarsHmilyCommunicatorBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * add HmilyCommunicatorBeanPostProcessor and override old old tars's bean post processor.
 *
 * @author tydhot
 */
@Configuration
public class TarsHmilyConfiguration {

    /**
     * add HmilyCommunicatorBeanPostProcessor.
     *
     * @param  communicator communicator
     * @return HmilyCommunicatorBeanPostProcessor
     */
    @Bean
    public TarsHmilyCommunicatorBeanPostProcessor hmilyCommunicatorBeanPostProcessor(final Communicator communicator) {
        return new TarsHmilyCommunicatorBeanPostProcessor(communicator);
    }

    /**
     * add TarsHmilyStartup.
     *
     * @return TarsHmilyStartup
     */
    @Bean
    public TarsHmilyFilterStartup tarsHmilyStartup() {
        return new TarsHmilyFilterStartup();
    }

}
