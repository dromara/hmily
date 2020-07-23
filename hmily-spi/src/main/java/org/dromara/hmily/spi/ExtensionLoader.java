/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.spi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Extension loader.
 * This is done by loading the properties file.
 * https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/common/extension/ExtensionLoader.java
 *
 * @author xiaoyu(Myth)
 */
@SuppressWarnings("all")
public final class ExtensionLoader<T> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionLoader.class);
    
    private static final String HMILY_DIRECTORY = "META-INF/hmily/";
    
    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();
    
    private final Holder<List<ExtensionEntity>> entitiesHolder = new Holder<>();
    
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    
    private final Map<String, ExtensionEntity> nameToEntityMap = new ConcurrentHashMap<>();
    
    private final Map<Class<?>, ExtensionEntity> classToEntityMap = new ConcurrentHashMap<>();
    
    private final Class<T> clazz;
    
    /**
     * Instantiates a new Extension loader.
     *
     * @param clazz the clazz.
     */
    private ExtensionLoader(final Class<T> clazz) {
        this.clazz = clazz;
    }
    
    /**
     * Gets extension loader.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the extension loader.
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("extension clazz is null");
        }
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension clazz (" + clazz + "is not interface!");
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) LOADERS.get(clazz);
        if (extensionLoader != null) {
            return extensionLoader;
        }
        LOADERS.putIfAbsent(clazz, new ExtensionLoader<>(clazz));
        return (ExtensionLoader<T>) LOADERS.get(clazz);
    }
    
    public T load(final ClassLoader loader) {
        return loadExtension(loader, null, null);
    }
    
    public T load(final String name, final ClassLoader loader) {
        return loadExtension(name, loader, null, null);
    }
    
    public T load(final String name, final Object[] args, final ClassLoader loader) {
        Class[] argsType = null;
        if (args != null && args.length > 0) {
            argsType = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argsType[i] = args[i].getClass();
            }
        }
        return loadExtension(name, loader, argsType, args);
    }
    
    public T load(final String name, final Class[] argsType, final Object[] args, final ClassLoader loader) {
        return loadExtension(name, loader, argsType, args);
    }
    
    public List<T> loadAll(final ClassLoader loader) {
        return loadAll(null, null, loader);
    }
    
    /**
     * get all implements
     *
     * @param argsType the args type
     * @param args     the args
     * @return list list
     */
    private List<T> loadAll(final Class[] argsType, final Object[] args, final ClassLoader loader) {
        List<Class<?>> allClazzs = getAllExtensionClass(loader);
        if (allClazzs.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return allClazzs.stream().map(t -> {
            ExtensionEntity extensionEntity = classToEntityMap.get(t);
            return getExtensionInstance(extensionEntity, loader, argsType, args);
        }).collect(Collectors.toList());
    }
    
    private List<Class<?>> getAllExtensionClass(final ClassLoader loader) {
        return loadAllExtensionClass(loader);
    }
    
    private T loadExtension(final ClassLoader loader, final Class<?>[] argTypes, final Object[] args) {
        loadAllExtensionClass(loader);
        ExtensionEntity extensionEntity = getDefaultExtensionEntity();
        return getExtensionInstance(extensionEntity, loader, argTypes, args);
    }
    
    private T loadExtension(final String name, final ClassLoader loader, final Class<?>[] argTypes, final Object[] args) {
        loadAllExtensionClass(loader);
        ExtensionEntity extensionEntity = getCachedExtensionEntity(name);
        return getExtensionInstance(extensionEntity, loader, argTypes, args);
    }
    
    private T getExtensionInstance(final ExtensionEntity entity, final ClassLoader loader, final Class[] argTypes, final Object[] args) {
        if (entity == null) {
            throw new IllegalStateException("not found service provider for : " + clazz.getName());
        }
        if (ScopeType.SINGLETON.equals(entity.getScopeType())) {
            Holder<Object> holder = cachedInstances.get(entity.getName());
            if (holder == null) {
                cachedInstances.putIfAbsent(entity.getName(), new Holder<>());
                holder = cachedInstances.get(entity.getName());
            }
            Object instance = holder.getValue();
            if (instance == null) {
                synchronized (holder) {
                    instance = holder.getValue();
                    if (instance == null) {
                        instance = createNewExtension(entity, loader, argTypes, args);
                        holder.setValue(instance);
                    }
                }
            }
            return (T) instance;
        } else {
            return createNewExtension(entity, loader, argTypes, args);
        }
    }
    
    private T createNewExtension(final ExtensionEntity entity, final ClassLoader loader, final Class<?>[] argTypes, final Object[] args) {
        try {
            return initInstance(entity.getServiceClass(), argTypes, args);
        } catch (Exception t) {
            throw new IllegalStateException("Extension instance(entity: " + entity + ", class: " + clazz + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }
    
    private ExtensionEntity getDefaultExtensionEntity() {
        List<ExtensionEntity> entitiesHolderValue = entitiesHolder.getValue();
        if (null != entitiesHolderValue && entitiesHolderValue.size() > 0) {
            return entitiesHolderValue.get(entitiesHolderValue.size() - 1);
        }
        return null;
    }
    
    private ExtensionEntity getCachedExtensionEntity(final String name) {
        return nameToEntityMap.get(name);
    }
    
    private List<Class<?>> loadAllExtensionClass(final ClassLoader loader) {
        List<ExtensionEntity> entityList = entitiesHolder.getValue();
        if (null == entityList) {
            synchronized (entitiesHolder) {
                entityList = entitiesHolder.getValue();
                if (null == entityList) {
                    entityList = findAllExtensionEntity(loader);
                    entitiesHolder.setValue(entityList);
                }
            }
        }
        return entityList.stream().map(ExtensionEntity::getServiceClass).collect(Collectors.toList());
    }
    
    private List<ExtensionEntity> findAllExtensionEntity(final ClassLoader loader) {
        List<ExtensionEntity> entityList = new ArrayList<>();
        loadDirectory(HMILY_DIRECTORY + clazz.getName(), loader, entityList);
        nameToEntityMap.values().stream().sorted(Comparator.comparing(ExtensionEntity::getOrder)).collect(Collectors.toList());
        return entityList.stream().sorted(Comparator.comparing(ExtensionEntity::getOrder)).collect(Collectors.toList());
    }
    
    private void loadDirectory(final String dir, final ClassLoader classLoader, final List<ExtensionEntity> entityList) {
        try {
            Enumeration<URL> urls = classLoader != null ? classLoader.getResources(dir) : ClassLoader.getSystemResources(dir);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    loadResources(entityList, url, classLoader);
                }
            }
        } catch (IOException t) {
            LOGGER.error("load extension class error {}", dir, t);
        }
    }
    
    private void loadResources(final List<ExtensionEntity> entityList, final URL url, final ClassLoader classLoader) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((k, v) -> {
                String name = (String) k;
                if (StringUtils.isNotBlank(name)) {
                    try {
                        loadClass(entityList, name, classLoader);
                    } catch (ClassNotFoundException e) {
                        LOGGER.warn("Load [{}] class fail. {}", name, e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("load extension resources error", e);
        }
    }
    
    private void loadClass(final List<ExtensionEntity> entityList, final String className, final ClassLoader loader) throws ClassNotFoundException {
        if (!containsClazz(className, loader)) {
            Class<?> clazz = Class.forName(className, true, loader);
            if (!clazz.isAssignableFrom(clazz)) {
                throw new IllegalStateException("load extension resources error," + clazz + " subtype is not of " + clazz);
            }
            String name = null;
            Integer order = 0;
            ScopeType scope = ScopeType.SINGLETON;
            HmilySPI hmilySPI = clazz.getAnnotation(HmilySPI.class);
            if (null != hmilySPI) {
                name = hmilySPI.value();
                order = hmilySPI.order();
                scope = hmilySPI.scopeType();
            }
            ExtensionEntity result = new ExtensionEntity(name, clazz, order, scope);
            entityList.add(result);
            classToEntityMap.put(clazz, result);
            if (null != name) {
                nameToEntityMap.put(name, result);
            }
        }
    }
    
    private boolean containsClazz(final String className, final ClassLoader loader) {
        return classToEntityMap.entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(className))
                .anyMatch(entry -> Objects.equals(entry.getValue().getServiceClass().getClassLoader(), loader));
    }
    
    private T initInstance(final Class<?> implClazz, final Class<?>[] argTypes, final Object[] args)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        T result = null;
        if (argTypes != null && args != null) {
            Constructor<?> constructor = implClazz.getDeclaredConstructor(argTypes);
            result = clazz.cast(constructor.newInstance(args));
        } else {
            result = clazz.cast(implClazz.newInstance());
        }
        if (result instanceof InitializeSPI) {
            ((InitializeSPI) result).init();
        }
        return result;
    }
    
    /**
     * The type Holder.
     *
     * @param <T> the type parameter.
     */
    public static class Holder<T> {
        
        private volatile T value;
        
        /**
         * Gets value.
         *
         * @return the value
         */
        public T getValue() {
            return value;
        }
        
        /**
         * Sets value.
         *
         * @param value the value
         */
        public void setValue(final T value) {
            this.value = value;
        }
    }
}
