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

package org.dromara.hmily.admin.controller;

import org.dromara.hmily.admin.dto.UserDTO;
import org.dromara.hmily.admin.service.LoginService;
import org.dromara.hmily.common.utils.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * login rest controller.
 *
 * @author xiaoyu(Myth)
 */
@RestController
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(final LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public AjaxResponse login(@RequestBody final UserDTO userDTO) {
        final Boolean login = loginService.login(userDTO.getUserName(), userDTO.getPassword());
        return AjaxResponse.success(login);
    }

    @PostMapping("/logout")
    public AjaxResponse logout() {
        return AjaxResponse.success(loginService.logout());
    }

}
