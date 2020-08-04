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

package org.dromara.hmily.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * The type Kryo pool factory.
 *
 * @author xiaoyu
 */
public final class KryoPoolFactory implements KryoFactory {
    
    private static final KryoPoolFactory FACTORY = new KryoPoolFactory();
    
    private KryoPool pool = new KryoPool.Builder(this).softReferences().build();
    
    private KryoPoolFactory() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static KryoPoolFactory getInstance() {
        return FACTORY;
    }
    
    /**
     * Get kryo.
     *
     * @return the kryo
     */
    public Kryo get() {
        return pool.borrow();
    }
    
    /**
     * Return kryo.
     *
     * @param kryo the kryo
     */
    public void returnKryo(final Kryo kryo) {
        if (Objects.nonNull(kryo)) {
            pool.release(kryo);
        }
    }
    
    @Override
    public Kryo create() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        //register serializer
        kryo.register(BigDecimal.class, new DefaultSerializers.BigDecimalSerializer());
        kryo.register(BigInteger.class, new DefaultSerializers.BigIntegerSerializer());
        return kryo;
    }
}
