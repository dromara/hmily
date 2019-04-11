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

package org.dromara.hmily.common.utils.extension;

import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.exception.HmilyException;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;


/**
 * The type Extension loader.
 *
 * @author xiaoyu
 */
public final class ExtensionLoader<T> {

    private Class<T> type;

    private ExtensionLoader(final Class<T> type) {
        this.type = type;
    }

    private static <T> boolean withExtensionAnnotation(final Class<T> type) {
        return type.isAnnotationPresent(HmilySPI.class);
    }

    /**
     * Gets extension loader.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the extension loader
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> type) {
        if (type == null) {
            throw new HmilyException("type == null");
        }
        if (!type.isInterface()) {
            throw new HmilyException("Extension type(" + type + ") not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new HmilyException("type" + type.getName() + "not exist");
        }
        return new ExtensionLoader<>(type);
    }

    /**
     * Gets activate extension.
     *
     * @param value the value
     * @return the activate extension
     */
    public T getActivateExtension(final String value) {
        ServiceLoader<T> loader = ServiceBootstrap.loadAll(type);
        return StreamSupport.stream(loader.spliterator(), false)
                .filter(e -> Objects.equals(e.getClass()
                        .getAnnotation(HmilySPI.class).value(), value))
                .findFirst().orElseThrow(() -> new HmilyException("Please check your configuration"));
    }

}
