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

package org.dromara.hmily.serializer.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.Serializer;
import com.caucho.hessian.io.SerializerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.dromara.hmily.spi.HmilySPI;

/**
 * HessianSerializer.
 *
 * @author xiaoyu
 */
@SuppressWarnings("unchecked")
@HmilySPI("hessian")
public class HessianSerializer implements HmilySerializer {
    
    @Override
    public byte[] serialize(final Object obj) throws HmilySerializerException {
        byte[] result;
        SerializerFactory hessian = HessianSerializerFactory.getInstance();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Serializer serializer = hessian.getSerializer(obj.getClass());
            Hessian2Output output = new Hessian2Output(bos);
            serializer.writeObject(obj, output);
            output.close();
            result = bos.toByteArray();
        } catch (IOException e) {
            throw new HmilySerializerException("Hessian serialize error " + e.getMessage());
        }
        return result;
    }
    
    @Override
    public <T> T deSerialize(final byte[] param, final Class<T> clazz) throws HmilySerializerException {
        T obj;
        try (ByteArrayInputStream is = new ByteArrayInputStream(param)) {
            Hessian2Input input = new Hessian2Input(is);
            obj = (T) input.readObject();
            input.close();
        } catch (IOException e) {
            throw new HmilySerializerException("Hessian deSerialize error " + e.getMessage());
        }
        return obj;
    }
}
