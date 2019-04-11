/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

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

/**
 * ServiceBootstrapTest.
 *
 * @author xiaoyu
 */
public class ServiceBootstrapTest {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBootstrapTest.class);

    @Test
    public void loadFirst() {
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