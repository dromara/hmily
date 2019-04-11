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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.exception.HmilyException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * KryoSerializer.
 *
 * @author xiaoyu
 */
@HmilySPI("kryo")
public class KryoSerializer implements ObjectSerializer {

    @Override
    public byte[] serialize(final Object obj) throws HmilyException {
        byte[] bytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); Output output = new Output(outputStream)) {
            //获取kryo对象
            Kryo kryo = new Kryo();
            kryo.writeObject(output, obj);
            bytes = output.toBytes();
            output.flush();
        } catch (IOException ex) {
            throw new HmilyException("kryo serialize error" + ex.getMessage());
        }
        return bytes;
    }

    @Override
    public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws HmilyException {
        T object;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(param)) {
            Kryo kryo = new Kryo();
            Input input = new Input(inputStream);
            object = kryo.readObject(input, clazz);
            input.close();
        } catch (IOException e) {
            throw new HmilyException("kryo deSerialize error" + e.getMessage());
        }
        return object;
    }

}
