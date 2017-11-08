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

package com.happylifeplat.tcc.admin.service.login;

import com.happylifeplat.tcc.admin.service.LoginService;
import com.happylifeplat.tcc.common.utils.LogUtil;
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
