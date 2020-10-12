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

package org.dromara.hmily.demo.brpc.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * DubboTccInventoryApplication.
 *
 * @author liu·yu
 */
@SpringBootApplication
@ImportResource({"classpath:applicationContext.xml"})
@MapperScan("org.dromara.hmily.demo.common.inventory.mapper")
public class BrpcHmilyInventoryApplication {

    /**
     * main.
     *
     * @param args args
     */
    public static void main(final String[] args) {
        SpringApplication springApplication = new SpringApplication(BrpcHmilyInventoryApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
        synchronized (BrpcHmilyInventoryApplication.class){
            try {
                BrpcHmilyInventoryApplication.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
