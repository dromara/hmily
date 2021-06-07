/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.config.loader.bind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.dromara.hmily.config.loader.property.ConfigPropertySource;
import org.dromara.hmily.config.loader.property.DefaultConfigPropertySource;
import org.dromara.hmily.config.loader.property.MapPropertyKeySource;
import org.dromara.hmily.config.loader.property.PropertyKeyParse;
import org.dromara.hmily.config.loader.property.PropertyKeySource;
import org.junit.Assert;
import org.junit.Test;

public final class CollectionBinderTest {

    @Test
    public void testArrayList() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.list[0]", 123);
        map.put("hmily.list[1]", 234);
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<CollectionPojo> data = BindData.of(DataType.of(CollectionPojo.class), CollectionPojo::new);
        CollectionPojo bind = binder.bind("hmily", data);
        Assert.assertEquals(bind.getList().get(0), 123);
        Assert.assertEquals(bind.getList().get(1), 234);
    }

    @Test
    public void testNotSetArrayList() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.list[0]", 123);
        map.put("hmily.list[1]", 234);
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<CollectionPojo2> data = BindData.of(DataType.of(CollectionPojo2.class), CollectionPojo2::new);
        CollectionPojo2 bind = binder.bind("hmily", data);
        Assert.assertNull(bind);
    }

    @Test
    public void testListGeneric() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.list2[0]", "123");
        map.put("hmily.list2[1]", 234);
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<CollectionPojo> data = BindData.of(DataType.of(CollectionPojo.class), CollectionPojo::new);
        CollectionPojo bind = binder.bind("hmily", data);
        Assert.assertEquals(bind.getList2().get(0), Integer.valueOf(123));
        Assert.assertEquals(bind.getList2().get(1), Integer.valueOf(234));
    }

    @Test
    public void testArray() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.intArray[0]", 123);
        map.put("hmily.intArray[1]", 234);
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<CollectionPojo> data = BindData.of(DataType.of(CollectionPojo.class), CollectionPojo::new);
        CollectionPojo bind = binder.bind("hmily", data);
        Assert.assertEquals(bind.getIntArray()[0], Integer.valueOf(123));
        Assert.assertEquals(bind.getIntArray()[1], Integer.valueOf(234));
    }


    @Data
    public static class CollectionPojo {

        private List list;

        private List<Integer> list2;

        private Integer[] intArray;
    }

    @Data
    public static class CollectionPojo2 {
        
        private List list;
    }
}
