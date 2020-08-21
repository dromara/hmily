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

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.property.PropertyName;

/**
 * The type Java bean binder.
 *
 * @author xiaoyu
 */
public class JavaBeanBinder extends BeanBinder {
    
    @Override
    public <T> T bind(final PropertyName name, final BindData<T> target, final Binder.Env env, final PropertyBinder propertyBinder) {
        boolean hasKnownBindableProperties = env.getSource().containsDescendantOf(name);
        Bean<T> bean = Bean.get(target, hasKnownBindableProperties);
        if (bean == null) {
            return null;
        }
        BeanSupplier<T> beanSupplier = bean.getSupplier(target);
        boolean bound = bind(propertyBinder, bean, beanSupplier);
        return bound ? beanSupplier.get() : null;
    }
    
    private <T> boolean bind(final PropertyBinder propertyBinder, final Bean<T> bean, final BeanSupplier<T> beanSupplier) {
        boolean bound = false;
        for (Map.Entry<String, BeanProperty> entry : bean.getProperties().entrySet()) {
            bound |= bind(beanSupplier, propertyBinder, entry.getValue());
        }
        return bound;
    }
    
    private <T> boolean bind(final BeanSupplier<T> beanSupplier, final PropertyBinder propertyBinder, final BeanProperty property) {
        String propertyName = property.getName();
        DataType type = property.getType().withGenerics(property.getField());
        Supplier<Object> value = property.getValue(beanSupplier);
        BindData<Object> bindData = BindData.of(type).withValue(value);
        Object bound = propertyBinder.bindProperty(propertyName, bindData);
        if (bound == null) {
            return false;
        }
        if (property.isSettable()) {
            property.setValue(beanSupplier, bound);
        } else if (value == null || !bound.equals(value.get())) {
            throw new IllegalStateException(
                    "No setter found for property: " + property.getName());
        }
        return true;
    }
    
    private static class Bean<T> {
        
        private static Bean<?> cached;
        
        private final Class<?> type;
        
        private final Map<String, BeanProperty> properties = new LinkedHashMap<>();
    
        /**
         * Instantiates a new Bean.
         *
         * @param type the type
         */
        Bean(final Class<?> type) {
            this.type = type;
            putProperties(type);
        }
        
        private void putProperties(final Class<?> clazz) {
            Class<?> type = clazz;
            while (type != null && !Object.class.equals(type)) {
                for (Method method : type.getDeclaredMethods()) {
                    if (isCandidate(method)) {
                        addMethod(method);
                    }
                }
                for (Field field : type.getDeclaredFields()) {
                    addField(field);
                }
                type = type.getSuperclass();
            }
        }
        
        private boolean isCandidate(final Method method) {
            int modifiers = method.getModifiers();
            return Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)
                    && !Modifier.isStatic(modifiers)
                    && !Object.class.equals(method.getDeclaringClass())
                    && !Class.class.equals(method.getDeclaringClass());
        }
        
        private void addMethod(final Method method) {
            addMethodIfPossible(method, "get", 0, BeanProperty::addGetter);
            addMethodIfPossible(method, "is", 0, BeanProperty::addGetter);
            addMethodIfPossible(method, "set", 1, BeanProperty::addSetter);
        }
        
        private void addMethodIfPossible(final Method method, final String prefix, final int parameterCount,
                                         final BiConsumer<BeanProperty, Method> consumer) {
            if (method.getParameterCount() == parameterCount
                    && method.getName().startsWith(prefix)
                    && method.getName().length() > prefix.length()) {
                String propertyName = Introspector
                        .decapitalize(method.getName().substring(prefix.length()));
                consumer.accept(this.properties.computeIfAbsent(propertyName,
                        this::getBeanProperty), method);
            }
        }
        
        private JavaBeanBinder.BeanProperty getBeanProperty(final String name) {
            return new BeanProperty(name);
        }
        
        private void addField(final Field field) {
            BeanProperty property = this.properties.get(field.getName());
            if (property != null) {
                property.addField(field);
            }
        }
    
        /**
         * Gets type.
         *
         * @return the type
         */
        Class<?> getType() {
            return this.type;
        }
    
        /**
         * Gets properties.
         *
         * @return the properties
         */
        Map<String, BeanProperty> getProperties() {
            return this.properties;
        }
    
        /**
         * Gets supplier.
         *
         * @param target the target
         * @return the supplier
         */
        @SuppressWarnings("unchecked")
        BeanSupplier<T> getSupplier(final BindData<T> target) {
            return new BeanSupplier<>(() -> {
                T instance = null;
                if (target.getValue() != null) {
                    instance = target.getValue().get();
                }
                if (instance == null) {
                    try {
                        instance = (T) this.type.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new ConfigException(e);
                    }
                }
                return instance;
            });
        }
    
        /**
         * Get bean.
         *
         * @param <T>             the type parameter
         * @param bindable        the bindable
         * @param canCallGetValue the can call get value
         * @return the bean
         */
        @SuppressWarnings("unchecked")
        static <T> Bean<T> get(final BindData<T> bindable, final boolean canCallGetValue) {
            Class<?> type = bindable.getType().getTypeClass();
            Supplier<T> value = bindable.getValue();
            T instance = null;
            if (canCallGetValue && value != null) {
                instance = value.get();
                type = (instance != null ? instance.getClass() : type);
            }
            if (instance == null && !isInstantiable(type)) {
                return null;
            }
            Bean<?> bean = Bean.cached;
            if (bean == null || !type.equals(bean.getType())) {
                bean = new Bean<>(type);
                cached = bean;
            }
            return (Bean<T>) bean;
        }
        
        private static boolean isInstantiable(final Class<?> type) {
            if (type.isInterface()) {
                return false;
            }
            try {
                type.getDeclaredConstructor();
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    private static class BeanSupplier<T> implements Supplier<T> {
        
        private final Supplier<T> factory;
        
        private T instance;
    
        /**
         * Instantiates a new Bean supplier.
         *
         * @param factory the factory
         */
        BeanSupplier(final Supplier<T> factory) {
            this.factory = factory;
        }
        
        @Override
        public T get() {
            if (this.instance == null) {
                this.instance = this.factory.get();
            }
            return this.instance;
        }
        
    }
    
    /**
     * A bean property being bound.
     */
    private static class BeanProperty {
        
        private final String name;
        
        private Method getter;
        
        private Method setter;
        
        private Field field;
    
        /**
         * Instantiates a new Bean property.
         *
         * @param name the name
         */
        BeanProperty(final String name) {
            this.name = name;
        }
    
        /**
         * Add getter.
         *
         * @param getter the getter
         */
        void addGetter(final Method getter) {
            if (this.getter == null) {
                this.getter = getter;
            }
        }
    
        /**
         * Add setter.
         *
         * @param setter the setter
         */
        void addSetter(final Method setter) {
            if (this.setter == null) {
                this.setter = setter;
            }
        }
    
        /**
         * Add field.
         *
         * @param field the field
         */
        void addField(final Field field) {
            if (this.field == null) {
                this.field = field;
            }
        }
    
        /**
         * Gets field.
         *
         * @return the field
         */
        public Field getField() {
            return field;
        }
    
        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return this.name;
        }
    
        /**
         * Gets type.
         *
         * @return the type
         */
        DataType getType() {
            if (this.setter != null) {
                Type[] parameterTypes = this.setter.getParameterTypes();
                if (parameterTypes.length > 0) {
                    return DataType.of(parameterTypes[0]);
                }
            }
            return DataType.of(this.getter.getReturnType());
        }
    
        /**
         * Gets value.
         *
         * @param instance the instance
         * @return the value
         */
        Supplier<Object> getValue(final Supplier<?> instance) {
            if (this.getter == null) {
                return null;
            }
            return () -> {
                try {
                    this.getter.setAccessible(true);
                    return this.getter.invoke(instance.get());
                } catch (Exception ex) {
                    throw new IllegalStateException(
                            "Unable to get value for property " + this.name, ex);
                }
            };
        }
    
        /**
         * Is settable boolean.
         *
         * @return the boolean
         */
        boolean isSettable() {
            return this.setter != null;
        }
    
        /**
         * Sets value.
         *
         * @param instance the instance
         * @param value    the value
         */
        void setValue(final Supplier<?> instance, final Object value) {
            try {
                this.setter.setAccessible(true);
                this.setter.invoke(instance.get(), value);
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to set value for property " + this.name, ex);
                
            }
        }
    }
}
