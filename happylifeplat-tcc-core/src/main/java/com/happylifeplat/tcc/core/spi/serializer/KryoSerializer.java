/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.spi.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.happylifeplat.tcc.common.enums.SerializeEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.core.spi.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class KryoSerializer implements ObjectSerializer {
    /**
     * 序列化
     *
     * @param obj 需要序更列化的对象
     * @return 序列化后的byte 数组
     * @throws TccException
     */
    @Override
    public byte[] serialize(Object obj) throws TccException {
        byte[] bytes;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            //获取kryo对象
            Kryo kryo = new Kryo();
            Output output = new Output(outputStream);
            kryo.writeObject(output, obj);
            bytes = output.toBytes();
            output.flush();
        } catch (Exception ex) {
            throw new TccException("kryo serialize error" + ex.getMessage());
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {

            }
        }
        return bytes;
    }

    /**
     * 反序列化
     *
     * @param param 需要反序列化的byte []
     * @return 序列化对象
     * @throws TccException
     */
    @Override
    public <T> T deSerialize(byte[] param, Class<T> clazz) throws TccException {
        T object;
        ByteArrayInputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(param);
            Kryo kryo = new Kryo();
            Input input = new Input(inputStream);
            object = kryo.readObject(input, clazz);
            input.close();
        } catch (Exception e) {
            throw new TccException("kryo deSerialize error" + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {

            }
        }
        return object;
    }

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return SerializeEnum.KRYO.getSerialize();
    }
}
