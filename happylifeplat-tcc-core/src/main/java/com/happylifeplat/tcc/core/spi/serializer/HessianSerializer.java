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

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.happylifeplat.tcc.common.enums.SerializeEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.core.spi.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @author xiaoyu
 */
public class HessianSerializer implements ObjectSerializer {
    @Override
    public byte[] serialize(Object obj) throws TccException {
        ByteArrayOutputStream baos;
        try {
            baos = new ByteArrayOutputStream();
            Hessian2Output hos = new Hessian2Output(baos);
            hos.writeObject(obj);
            hos.flush();
            hos.close();
        } catch (IOException ex) {
            throw new TccException("Hessian serialize error " + ex.getMessage());
        }
        return baos.toByteArray();
    }

    @Override
    public <T> T deSerialize(byte[] param, Class<T> clazz) throws TccException {
        ByteArrayInputStream bios;
        try {
            bios = new ByteArrayInputStream(param);
            Hessian2Input his = new Hessian2Input(bios);
            return (T) his.readObject();
        } catch (IOException e) {
            throw new TccException("Hessian deSerialize error " + e.getMessage());
        }
    }

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return SerializeEnum.HESSIAN.getSerialize();
    }
}
