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
        byte[] bytes = messagePackSerializer.serialize(Integer.valueOf(1));
        Assert.assertEquals(messagePackSerializer.deSerialize(bytes, Integer.class), Integer.valueOf(1));
    }
}
