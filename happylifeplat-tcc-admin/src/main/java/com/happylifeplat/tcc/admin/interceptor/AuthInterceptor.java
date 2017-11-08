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

package com.happylifeplat.tcc.admin.interceptor;

import com.happylifeplat.tcc.admin.service.login.LoginServiceImpl;
import com.happylifeplat.tcc.admin.annotation.Permission;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/23 20:08
 * @since JDK 1.8
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = ((HandlerMethod) handler);
            Method method = handlerMethod.getMethod();
            final Permission annotation = method.getAnnotation(Permission.class);
            if (Objects.isNull(annotation)) {
                return Boolean.TRUE;
            }
            final boolean login = annotation.isLogin();
            if (login) {
                if (!LoginServiceImpl.LOGIN_SUCCESS) {
                    request.setAttribute("code","404");
                    request.setAttribute("msg", "请您先登录！");
                    request.getRequestDispatcher("/").forward(request, response);
                    return Boolean.FALSE;
                }
            }
        }
        return super.preHandle(request, response, handler);
    }


}
