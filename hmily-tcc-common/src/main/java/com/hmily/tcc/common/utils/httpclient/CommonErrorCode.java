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

package com.hmily.tcc.common.utils.httpclient;

import org.apache.commons.lang3.StringUtils;

/**
 * 错误码定义类.
 * @author xiaoyu
 **/
public class CommonErrorCode {

    /**
     * 操作失败全局定义定义.
     */
    public static final int ERROR = -2;

    /**
     * 成功.
     */
    public static final int SUCCESSFUL = 200;

    /**
     * 获取错误码描述信息.
     *
     * @param code 错误码枚举的name,指向自定义的信息
     * @return 描述信息
     */
    public static String getErrorMsg(final int code) {
        //获取错误信息
        String msg = System.getProperty(String.valueOf(code));
        if (StringUtils.isBlank(msg)) {
            return "根据传入的错误码[" + code + "]没有找到相应的错误信息.";
        }
        return msg;
    }
}
