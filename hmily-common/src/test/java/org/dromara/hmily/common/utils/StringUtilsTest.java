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

package org.dromara.hmily.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsNoneBlank() {
        Assert.assertFalse(StringUtils.isNoneBlank((CharSequence) null));
        Assert.assertFalse(StringUtils.isNoneBlank(""));
        Assert.assertFalse(StringUtils.isNoneBlank("", "", ""));
        Assert.assertTrue(StringUtils.isNoneBlank("a", "b", "c"));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(true);
        Assert.assertTrue(StringUtils.isEmpty(new String[0]));
        Assert.assertFalse(StringUtils.isEmpty(new String[]{"a", "b", "c"}));
    }

    @Test
    public void testIsBlank() {
        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank(" "));
        Assert.assertFalse(StringUtils.isBlank("foobar"));
        Assert.assertFalse(StringUtils.isBlank("foo bar"));
    }
}
