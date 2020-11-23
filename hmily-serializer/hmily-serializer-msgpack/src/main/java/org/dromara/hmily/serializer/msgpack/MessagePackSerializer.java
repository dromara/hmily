/*
 * Copyright 20120 Dromara.org.
 *
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

package org.dromara.hmily.serializer.msgpack;

import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.dromara.hmily.spi.HmilySPI;
import org.msgpack.MessagePack;

import java.io.IOException;

/**
 * MessagePackSerializer.
 *
 * @author dongzl
 */
@HmilySPI("msgpack")
public class MessagePackSerializer implements HmilySerializer {
    
    private static final MessagePack MESSAGE = new MessagePack();
    
    @Override
    public byte[] serialize(final Object obj) throws HmilySerializerException {
        byte[] result;
        try {
            result = MESSAGE.write(obj);
        } catch (IOException e) {
            throw new HmilySerializerException("MessagePack serialize error " + e.getMessage());
        }
        return result;
    }
    
    @Override
    public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws HmilySerializerException {
        T obj;
        try {
            obj = MESSAGE.read(param, clazz);
        } catch (IOException e) {
            throw new HmilySerializerException("MessagePack deSerialize error " + e.getMessage());
        }
        return obj;
    }
}
