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
package com.hmily.tcc.common.serializer;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * @author xiaoyu
 */
public class SchemaCache {
    private static class SchemaCacheHolder {
        private static SchemaCache cache = new SchemaCache();
    }

    public static SchemaCache getInstance() {
        return SchemaCacheHolder.cache;
    }

    private Cache<Class<?>, Schema<?>> cache = CacheBuilder.newBuilder()
            .maximumSize(1024).expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private Schema<?> get(final Class<?> cls, Cache<Class<?>, Schema<?>> cache) {
        try {
            return cache.get(cls, () -> RuntimeSchema.createFrom(cls));
        } catch (ExecutionException e) {
            return null;
        }
    }

    public Schema<?> get(final Class<?> cls) {
        return get(cls, cache);
    }
}

