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

package org.dromara.hmily.demo.common.inventory.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * The type Inventory dto.
 *
 * @author xiaoyu
 */
@Data
public class InventoryDTO implements Serializable {

    private static final long serialVersionUID = 8229355519336565493L;

    /**
     * 商品id.
     */
    private String productId;

    /**
     * 数量.
     */
    private Integer count;
}
