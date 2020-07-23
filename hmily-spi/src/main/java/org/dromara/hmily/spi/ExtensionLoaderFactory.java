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

import java.util.List;

/**
 * The type Extension loader factory.
 */
public class ExtensionLoaderFactory {
    
    /**
     * Load t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @return the t
     */
    public static <T> T load(final Class<T> service) {
        return ExtensionLoader.getExtensionLoader(service).load(findClassLoader());
    }
    
    /**
     * Load t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @param name    the name
     * @return the t
     */
    public static <T> T load(final Class<T> service, final String name) {
        return ExtensionLoader.getExtensionLoader(service).load(name, findClassLoader());
    }
    
    /**
     * Load t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @param loader  the loader
     * @return the t
     */
    public static <T> T load(final Class<T> service, final ClassLoader loader) {
        return ExtensionLoader.getExtensionLoader(service).load(loader);
    }
    
    /**
     * Load t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @param name    the name
     * @param loader  the loader
     * @return the t
     */
    public static <T> T load(final Class<T> service, final String name, final ClassLoader loader) {
        return ExtensionLoader.getExtensionLoader(service).load(name, loader);
    }
    
    /**
     * Load t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @param name    the name
     * @param args    the args
     * @return the t
     */
    public static <T> T load(final Class<T> service, final String name, final Object[] args) {
        return ExtensionLoader.getExtensionLoader(service).load(name, args, findClassLoader());
    }
    
    /**
     * Load t.
     *
     * @param <T>      the type parameter
     * @param service  the service
     * @param name     the name
     * @param argsType the args type
     * @param args     the args
     * @return the t
     */
    public static <T> T load(final Class<T> service, final String name, final Class<?>[] argsType, final Object[] args) {
        return ExtensionLoader.getExtensionLoader(service).load(name, argsType, args, findClassLoader());
    }
    
    /**
     * Load all list.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @return the list
     */
    public static <T> List<T> loadAll(final Class<T> service) {
        return ExtensionLoader.getExtensionLoader(service).loadAll(findClassLoader());
    }
    
    private static ClassLoader findClassLoader() {
        return ExtensionLoaderFactory.class.getClassLoader();
    }
}
