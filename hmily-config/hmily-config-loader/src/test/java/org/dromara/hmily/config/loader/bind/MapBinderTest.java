/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dromara.hmily.config.loader.bind;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.dromara.hmily.config.loader.property.ConfigPropertySource;
import org.dromara.hmily.config.loader.property.DefaultConfigPropertySource;
import org.dromara.hmily.config.loader.property.MapPropertyKeySource;
import org.dromara.hmily.config.loader.property.PropertyKeyParse;
import org.dromara.hmily.config.loader.property.PropertyKeySource;
import org.junit.Assert;
import org.junit.Test;

public class MapBinderTest {

    @Test
    public void testMap() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.map.userName", "sixh");
        map.put("hmily.map.passWord", 123456);
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<MapPojo> data = BindData.of(DataType.of(MapPojo.class), MapPojo::new);
        MapPojo bind = binder.bind("hmily", data);
        Assert.assertEquals(bind.getMap().get("userName"), map.get("hmily.map.userName"));
        Assert.assertEquals(bind.getMap().get("passWord"), map.get("hmily.map.passWord"));
        System.out.println(bind);
    }

    @Test
    public void testMapGeneric() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.map2.userName", "456");
        map.put("hmily.map2.passWord", 123456);
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<MapPojo> data = BindData.of(DataType.of(MapPojo.class), MapPojo::new);
        MapPojo bind = binder.bind("hmily", data);
        Assert.assertEquals(bind.getMap2().get("userName"), Integer.valueOf(456));
        Assert.assertEquals(bind.getMap2().get("passWord"), map.get("hmily.map2.passWord"));
        System.out.println(bind);
    }

    @Data
    public static class MapPojo {
        
        private Map map;

        private Map<String,Integer> map2;
    }
}
