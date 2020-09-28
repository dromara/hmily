package org.dromara.hmily.tars.spring;

import com.qq.tars.client.Communicator;
import com.qq.tars.client.CommunicatorConfig;
import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.common.util.StringUtils;
import com.qq.tars.protocol.annotation.Servant;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.spring.annotation.TarsClient;
import org.dromara.hmily.tars.loadbalance.HmilyLoadBalance;
import org.dromara.hmily.tars.loadbalance.HmilyRoundRobinLoadBalance;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * add HmilyCommunicatorBeanPostProcessor and override old tars's bean post processor.
 *
 * @author tydhot
 */
public class TarsHmilyCommunicatorBeanPostProcessor implements MergedBeanDefinitionPostProcessor {

    private final Communicator communicator;

    public TarsHmilyCommunicatorBeanPostProcessor(final Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        Class clazz = bean.getClass();
        processFields(bean, clazz.getDeclaredFields());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    private void processFields(final Object bean, final Field[] declaredFields) {
        for (Field field : declaredFields) {
            TarsClient annotation = AnnotationUtils.getAnnotation(field, TarsClient.class);
            if (annotation == null) {
                continue;
            }

            if (field.getType().getAnnotation(Servant.class) == null) {
                throw new RuntimeException("[TARS] autowire client failed: target field is not  tars  client");
            }

            String objName = annotation.name();

            if (StringUtils.isEmpty(annotation.value())) {
                throw new RuntimeException("[TARS] autowire client failed: objName is empty");
            }

            ServantProxyConfig config = new ServantProxyConfig(objName);
            CommunicatorConfig communicatorConfig = ConfigurationManager.getInstance().getServerConfig().getCommunicatorConfig();
            config.setModuleName(communicatorConfig.getModuleName(), communicatorConfig.isEnableSet(), communicatorConfig.getSetDivision());
            if (StringUtils.isNotEmpty(communicatorConfig.getSetDivision())) {
                config.setSetDivision(communicatorConfig.getSetDivision());
            }
            if (StringUtils.isNotEmpty(annotation.setDivision())) {
                config.setSetDivision(communicatorConfig.getSetDivision());
                config.setEnableSet(annotation.enableSet());
            }
            config.setConnections(annotation.connections());
            config.setConnectTimeout(annotation.connectTimeout());
            config.setSyncTimeout(annotation.syncTimeout());
            config.setAsyncTimeout(annotation.asyncTimeout());
            config.setTcpNoDelay(annotation.tcpNoDelay());
            config.setCharsetName(annotation.charsetName());

            Object proxy = communicator.stringToProxy(field.getType(),
                    config,
                    new HmilyLoadBalance(new HmilyRoundRobinLoadBalance(config), config));

            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, proxy);
        }
    }

    @Override
    public void postProcessMergedBeanDefinition(final RootBeanDefinition rootBeanDefinition, final Class<?> aClass, final String s) {

    }
}
