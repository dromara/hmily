package com.hmily.tcc.common.utils;

/**
 * DefaultValueUtils.
 *
 * @author xiaoyu
 */
@SuppressWarnings("all")
public class DefaultValueUtils {

    private static final int ZERO = 0;

    /**
     * return default object.
     *
     * @param type class
     * @return Object
     */
    public static Object getDefaultValue(final Class type) {
        if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type) || Byte.class.equals(type)) {
            return ZERO;
        } else if (short.class.equals(type) || Short.class.equals(type)) {
            return ZERO;
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return ZERO;
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            return 0L;
        } else if (float.class.equals(type) || Float.class.equals(type)) {
            return 0.0f;
        } else if (double.class.equals(type) || Double.class.equals(type)) {
            return 0.0d;
        } else if (String.class.equals(type)) {
            return "";
        }
        return new Object();
    }

}
