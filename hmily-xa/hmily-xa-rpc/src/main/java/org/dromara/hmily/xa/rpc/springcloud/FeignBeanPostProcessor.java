package org.dromara.hmily.xa.rpc.springcloud;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Proxy;

/**
 * 因为用户可能自己调用rest template，所以这样更好
 */
public class FeignBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //代理Feign
        Class<?> beanClass = bean.getClass ();
        //findAnnotation保证找到接口的注解
        FeignClient feignClient = AnnotationUtils.findAnnotation (beanClass, FeignClient.class);
        if (feignClient != null) {
            return Proxy.newProxyInstance (beanClass.getClassLoader (), beanClass.getInterfaces (),
                    new FeignRequestInvocationHandler (bean));
        }
        return bean;
    }
}
