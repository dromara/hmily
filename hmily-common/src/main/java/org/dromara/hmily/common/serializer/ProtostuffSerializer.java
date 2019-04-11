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

package org.dromara.hmily.common.serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.exception.HmilyException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ProtostuffSerializer.
 *
 * @author xiaoyu
 */
@SuppressWarnings("unchecked")
@HmilySPI("protostuff")
public class ProtostuffSerializer implements ObjectSerializer {

    private static final SchemaCache CACHED_SCHEMA = SchemaCache.getInstance();

    private static final Objenesis OBJENESIS = new ObjenesisStd(true);

    private static <T> Schema<T> getSchema(final Class<T> cls) {
        return (Schema<T>) CACHED_SCHEMA.get(cls);
    }

    @Override
    public byte[] serialize(final Object obj) throws HmilyException {
        Class cls = obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Schema schema = getSchema(cls);
            ProtostuffIOUtil.writeTo(outputStream, obj, schema, buffer);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new HmilyException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws HmilyException {
        T object;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(param)) {
            object = OBJENESIS.newInstance(clazz);
            Schema schema = getSchema((Class) clazz);
            ProtostuffIOUtil.mergeFrom(inputStream, object, schema);
            return object;
        } catch (IOException e) {
            throw new HmilyException(e.getMessage(), e);
        }
    }
}

