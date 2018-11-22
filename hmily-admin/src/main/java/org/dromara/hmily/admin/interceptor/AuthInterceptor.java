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

package org.dromara.hmily.admin.interceptor;

import org.dromara.hmily.admin.annotation.Permission;
import org.dromara.hmily.admin.service.login.LoginServiceImpl;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * AuthInterceptor.
 *
 * @author xiaoyu(Myth)
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            final Permission annotation = method.getAnnotation(Permission.class);
            if (Objects.isNull(annotation)) {
                return Boolean.TRUE;
            }
            final boolean login = annotation.isLogin();
            if (login && !LoginServiceImpl.LOGIN_SUCCESS) {
                request.setAttribute("code", "404");
                request.setAttribute("msg", "请您先登录！");
                request.getRequestDispatcher("/").forward(request, response);
                return Boolean.FALSE;
            }
        }
        return super.preHandle(request, response, handler);
    }

}
