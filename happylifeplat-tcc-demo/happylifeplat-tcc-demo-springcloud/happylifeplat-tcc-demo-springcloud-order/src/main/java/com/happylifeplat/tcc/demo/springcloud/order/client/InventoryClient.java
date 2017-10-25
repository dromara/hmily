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
package com.happylifeplat.tcc.demo.springcloud.order.client;

import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.demo.springcloud.order.configuration.MyConfiguration;
import com.happylifeplat.tcc.demo.springcloud.order.dto.InventoryDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 使用FeignClient，用于通知Feign组件对该接口进行代理，不需要编写接口实现，使用者直接通过@Autowire注入，
 * Spring Cloud应用在启动时，Feign会扫描标有@FeignClient注解的接口，生成代理，并注册到Spring容器中，生成代理时Feign会为每个接口方法创建一个RequetTemplate对象，该对象封装了HTTP请求需要的全部信息，请求参数名、请求方法等信息都是在这个过程中确定的，Feign的模板化就体现在这里
 * @FeignClient(name = "inventory-service")意为通知Feign在调用该接口方法时要向Eureka中查询名为inventory-service的服务，从而得到服务URL
 * @author mqzhao
 *
 */
@FeignClient(value = "inventory-service",configuration =MyConfiguration.class)
public interface InventoryClient {

    @Tcc
    @RequestMapping("/inventory-service/inventory/decrease")
    Boolean decrease(@RequestBody  InventoryDTO inventoryDTO);

    @Tcc
    @RequestMapping("/inventory-service/inventory/mockWithTryException")
    Boolean mockWithTryException(@RequestBody  InventoryDTO inventoryDTO);


    @Tcc
    @RequestMapping("/inventory-service/inventory/mockWithTryTimeout")
    Boolean mockWithTryTimeout(@RequestBody  InventoryDTO inventoryDTO);
}
