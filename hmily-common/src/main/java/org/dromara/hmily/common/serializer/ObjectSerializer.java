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

import org.dromara.hmily.common.exception.HmilyException;

/**
 * ObjectSerializer.
 * @author xiaoyu
 */
public interface ObjectSerializer {

    /**
     * 序列化对象.
     *
     * @param obj 需要序更列化的对象
     * @return byte []
     * @throws HmilyException 异常信息
     */
    byte[] serialize(Object obj) throws HmilyException;


    /**
     * 反序列化对象.
     *
     * @param param 需要反序列化的byte []
     * @param clazz java对象
     * @param <T>   泛型支持
     * @return 对象
     * @throws HmilyException 异常信息
     */
    <T> T deSerialize(byte[] param, Class<T> clazz) throws HmilyException;

    /**
     * 设置scheme.
     *
     * @return scheme 命名
     */
    String getScheme();
}
