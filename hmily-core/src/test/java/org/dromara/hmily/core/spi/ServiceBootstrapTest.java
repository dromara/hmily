package org.dromara.hmily.core.spi;


import org.dromara.hmily.common.enums.SerializeEnum;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.ServiceBootstrap;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;


public class ServiceBootstrapTest {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBootstrapTest.class);


    @Test
    public void loadFirst() throws Exception {
        final ObjectSerializer objectSerializer = ServiceBootstrap.loadFirst(ObjectSerializer.class);
        LOGGER.info("加载的序列化名称为：{}", objectSerializer.getClass().getName());

    }


    @Test
    public void loadAll() {
        //spi  serialize
        final SerializeEnum serializeEnum = SerializeEnum.HESSIAN;
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);

        final Optional<ObjectSerializer> serializer = StreamSupport.stream(objectSerializers.spliterator(), false)
                .filter(objectSerializer ->
                        Objects.equals(objectSerializer.getScheme(), serializeEnum.getSerialize())).findFirst();

        serializer.ifPresent(objectSerializer -> LOGGER.info("加载的序列化名称为：{}", objectSerializer.getClass().getName()));


    }

}