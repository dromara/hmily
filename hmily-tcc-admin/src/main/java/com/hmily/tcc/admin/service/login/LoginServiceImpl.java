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

package com.hmily.tcc.admin.service.login;

import com.hmily.tcc.admin.service.LoginService;
import com.hmily.tcc.common.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/20 10:19
 * @since JDK 1.8
 */
@Service("loginService")
public class LoginServiceImpl implements LoginService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServiceImpl.class);


    @Value("${tcc.admin.userName}")
    private String userName;

    @Value("${tcc.admin.password}")
    private String password;

    public static boolean LOGIN_SUCCESS = false;


    /**
     * 登录接口，验证用户名 密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return true 成功
     */
    @Override
    public Boolean login(String userName, String password) {
        LogUtil.info(LOGGER, "输入的用户名密码为:{}", () -> userName + "," + password);
        if (userName.equals(this.userName) && password.equals(this.password)) {
            LOGIN_SUCCESS = true;
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 用户登出
     *
     * @return true 成功
     */
    @Override
    public Boolean logout() {
        LOGIN_SUCCESS = false;
        return Boolean.TRUE;
    }
}
