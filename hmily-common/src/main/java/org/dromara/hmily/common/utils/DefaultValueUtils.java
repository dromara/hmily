package org.dromara.hmily.common.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * DefaultValueUtils.
 *
 * @author xiaoyu
 */
@SuppressWarnings("all")
public class DefaultValueUtils {

    private static final int ZERO = 0;

    /**
     * Gets default value.
     *
     * @param clazz the clazz
     * @return the default value
     * @throws IllegalAccessException    the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws InstantiationException    the instantiation exception
     */
    public static Object getDefaultValue(final Class clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
            return false;
        } else if (byte.class.equals(clazz) || Byte.class.equals(clazz)) {
            return ZERO;
        } else if (short.class.equals(clazz) || Short.class.equals(clazz)) {
            return ZERO;
        } else if (int.class.equals(clazz) || Integer.class.equals(clazz)) {
            return ZERO;
        } else if (long.class.equals(clazz) || Long.class.equals(clazz)) {
            return 0L;
        } else if (float.class.equals(clazz) || Float.class.equals(clazz)) {
            return 0.0f;
        } else if (double.class.equals(clazz) || Double.class.equals(clazz)) {
            return 0.0d;
        } else if (String.class.equals(clazz)) {
            return "";
        } else if (Void.TYPE.equals(clazz)) {
            return "";
        }

        final Constructor[] constructors = clazz.getDeclaredConstructors();
        Constructor constructor = constructors[constructors.length - 1];
        constructor.setAccessible(true);
        final Class[] parameClasses = constructor.getParameterTypes();
        Object[] args = new Object[parameClasses.length];
        for (int i = 0; i < parameClasses.length; i++) {
            Class clazzes = parameClasses[i];
            if (clazzes.isPrimitive()) {
                args[i] = 0;
            } else {
                args[i] = null;
            }
        }
        return constructor.newInstance(args);
    }

}
