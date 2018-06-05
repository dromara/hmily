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

package com.hmily.tcc.common.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hmily.tcc.common.enums.SerializeEnum;
import com.hmily.tcc.common.exception.TccException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * KryoSerializer.
 * @author xiaoyu
 */
public class KryoSerializer implements ObjectSerializer {

    /**
     * 序列化.
     *
     * @param obj 需要序更列化的对象
     * @return 序列化后的byte 数组
     * @throws TccException 异常
     */
    @Override
    public byte[] serialize(final Object obj) throws TccException {
        byte[] bytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); Output output = new Output(outputStream)) {
            //获取kryo对象
            Kryo kryo = new Kryo();
            kryo.writeObject(output, obj);
            bytes = output.toBytes();
            output.flush();
        } catch (IOException ex) {
            throw new TccException("kryo serialize error" + ex.getMessage());
        }
        return bytes;
    }

    /**
     * 反序列化.
     *
     * @param param 需要反序列化的byte []
     * @return 序列化对象
     * @throws TccException 异常
     */
    @Override
    public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws TccException {
        T object;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(param)) {
            Kryo kryo = new Kryo();
            Input input = new Input(inputStream);
            object = kryo.readObject(input, clazz);
            input.close();
        } catch (IOException e) {
            throw new TccException("kryo deSerialize error" + e.getMessage());
        }
        return object;
    }

    /**
     * 设置scheme.
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return SerializeEnum.KRYO.getSerialize();
    }
}
