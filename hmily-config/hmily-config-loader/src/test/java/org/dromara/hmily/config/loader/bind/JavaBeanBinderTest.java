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


@Data
public class JavaBeanBinderTest {

    private String stringTest;

    private Integer integerTest;

    private Double doubleTest;

    private Long longTest;

    private Character chartTest;

    private Float floatTest;

    private Boolean boolTest;

    @Test
    public void testJavaBeanBind() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.stringTest", "123");
        map.put("hmily.integerTest", 456);
        map.put("hmily.doubleTest", 42.12);
        map.put("hmily.longTest", 100L);
        map.put("hmily.chartTest", 'a');
        map.put("hmily.floatTest", 12.1F);
        map.put("hmily.boolTest", true);
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<JavaBeanBinderTest> data = BindData.of(DataType.of(JavaBeanBinderTest.class), JavaBeanBinderTest::new);
        JavaBeanBinderTest bind = binder.bind("hmily", data);
        Assert.assertEquals(map.get("hmily.stringTest"), bind.getStringTest());
        Assert.assertEquals(map.get("hmily.integerTest"), bind.getIntegerTest());
        Assert.assertEquals(map.get("hmily.doubleTest"), bind.getDoubleTest());
        Assert.assertEquals(map.get("hmily.chartTest"), bind.getChartTest());
        Assert.assertEquals(map.get("hmily.longTest"), bind.getLongTest());
        Assert.assertEquals(map.get("hmily.floatTest"), bind.getFloatTest());
        Assert.assertEquals(map.get("hmily.boolTest"), bind.getBoolTest());
    }

    @Test
    public void testJavaBeanBindParse() {
        String name = "hmily.yml";
        Map<String, Object> map = new HashMap<>();
        map.put("hmily.stringTest", 123);
        map.put("hmily.integerTest", "123");
        map.put("hmily.doubleTest", "123");
        map.put("hmily.longTest", "123");
        map.put("hmily.chartTest", "A");
        map.put("hmily.floatTest", "123");
        map.put("hmily.boolTest", "true");
        PropertyKeySource<?> propertySource = new MapPropertyKeySource(name, map);
        ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertySource, PropertyKeyParse.INSTANCE);
        Binder binder = Binder.of(configPropertySource);
        BindData<JavaBeanBinderTest> data = BindData.of(DataType.of(JavaBeanBinderTest.class), JavaBeanBinderTest::new);
        JavaBeanBinderTest bind = binder.bind("hmily", data);
        Assert.assertEquals("123", bind.getStringTest());
        Assert.assertEquals(Integer.valueOf(123), bind.getIntegerTest());
        Assert.assertEquals(Double.valueOf(123), bind.getDoubleTest());
        Assert.assertEquals(Character.valueOf('A'), bind.getChartTest());
        Assert.assertEquals(Long.valueOf(123), bind.getLongTest());
        Assert.assertEquals(Float.valueOf(123), bind.getFloatTest());
        Assert.assertEquals(Boolean.TRUE, bind.getBoolTest());
    }
}
