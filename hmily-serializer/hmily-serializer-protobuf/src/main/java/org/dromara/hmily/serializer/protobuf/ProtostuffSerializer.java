/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.serializer.protobuf;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.dromara.hmily.spi.HmilySPI;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * ProtostuffSerializer.
 *
 * @author xiaoyu
 */
@SuppressWarnings("all")
@HmilySPI("protostuff")
public class ProtostuffSerializer implements HmilySerializer {

    private static final SchemaCache CACHED_SCHEMA = SchemaCache.getInstance();

    private static final Objenesis OBJENESIS = new ObjenesisStd(true);

    private static <T> Schema<T> getSchema(final Class<T> cls) {
        return (Schema<T>) CACHED_SCHEMA.get(cls);
    }

    @Override
    public byte[] serialize(final Object obj) throws HmilySerializerException {
        Class<?> cls = obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Schema schema = getSchema(cls);
            ProtostuffIOUtil.writeTo(outputStream, obj, schema, buffer);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new HmilySerializerException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws HmilySerializerException {
        T object;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(param)) {
            object = OBJENESIS.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(inputStream, object, schema);
            return object;
        } catch (IOException e) {
            throw new HmilySerializerException(e.getMessage(), e);
        }
    }
}

