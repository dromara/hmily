package org.dromara.hmily.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsNoneBlank() {
        Assert.assertFalse(StringUtils.isNoneBlank(null));
        Assert.assertFalse(StringUtils.isNoneBlank(""));
        Assert.assertFalse(StringUtils.isNoneBlank("", "", ""));

        Assert.assertTrue(StringUtils.isNoneBlank("a", "b", "c"));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(null));
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
