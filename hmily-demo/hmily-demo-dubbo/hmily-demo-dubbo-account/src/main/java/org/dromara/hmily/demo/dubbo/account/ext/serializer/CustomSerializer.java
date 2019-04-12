package org.dromara.hmily.demo.dubbo.account.ext.serializer;

import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.serializer.ObjectSerializer;

/**
 * @author xiaoyu(Myth)
 */
@HmilySPI("custom")
public class CustomSerializer implements ObjectSerializer {

    @Override
    public byte[] serialize(Object obj) throws HmilyException {
        return new byte[0];
    }

    @Override
    public <T> T deSerialize(byte[] param, Class<T> clazz) throws HmilyException {
        return null;
    }
}
