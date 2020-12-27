/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dromara.hmily.serializer.msgpack;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * The type MessagePack serializer test.
 *
 * @author dongzl
 */
@RunWith(PowerMockRunner.class)
public class MessagePackSerializerTest {
    
    private static MessagePackSerializer messagePackSerializer;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        messagePackSerializer = new MessagePackSerializer(); 
    }
    
    @Test
    public void testSerialize() {
        byte[] bytes = messagePackSerializer.serialize("hmily");
        Assert.assertEquals(messagePackSerializer.deSerialize(bytes, String.class), "hmily");
    }
    
    @Test
    public void testDeSerialize() {
        byte[] bytes = messagePackSerializer.serialize(1);
        Assert.assertEquals(messagePackSerializer.deSerialize(bytes, Integer.class), Integer.valueOf(1));
    }
}
