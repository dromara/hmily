/*
 *
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.dromara.hmily.config.loader;

import java.util.List;
import java.util.function.Supplier;
import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.loader.bind.BindData;
import org.dromara.hmily.config.loader.bind.Binder;
import org.dromara.hmily.config.loader.bind.DataType;
import org.dromara.hmily.config.loader.property.ConfigPropertySource;
import org.dromara.hmily.config.loader.property.DefaultConfigPropertySource;
import org.dromara.hmily.config.loader.property.PropertyKeyParse;
import org.dromara.hmily.config.loader.property.PropertyKeySource;

/**
 * ConfigLoader.
 *
 * @param <T> the type parameter
 * @author xiaoyu
 */
public interface ConfigLoader<T extends Config> {
    
    /**
     * Load related configuration information.
     *
     * @param context the context
     * @param handler the handler
     */
    void load(Supplier<Context> context, LoaderHandler<T> handler);
    
    /**
     * Again load.
     *
     * @param context the context
     * @param handler the handler
     * @param tClass  the t class
     */
    default void againLoad(Supplier<Context> context, LoaderHandler<T> handler, Class<T> tClass) {
        T config = ConfigEnv.getInstance().getConfig(tClass);
        for (PropertyKeySource<?> propertyKeySource : context.get().getSource()) {
            ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertyKeySource, PropertyKeyParse.INSTANCE);
            Binder binder = Binder.of(configPropertySource);
            T bind = binder.bind(config.prefix(), BindData.of(DataType.of(tClass), () -> config));
            handler.finish(context, bind);
        }
    }
    
    /**
     * The type Context.
     */
    class Context {

        private ConfigLoader<Config> original;

        private List<PropertyKeySource<?>> propertyKeySources;
    
        /**
         * Instantiates a new Context.
         */
        public Context() {
        }
    
        /**
         * Instantiates a new Context.
         *
         * @param propertyKeySources the property key sources
         */
        public Context(List<PropertyKeySource<?>> propertyKeySources) {
            this(null, propertyKeySources);
        }
    
        /**
         * Instantiates a new Context.
         *
         * @param original           the original
         * @param propertyKeySources the property key sources
         */
        public Context(ConfigLoader<Config> original, List<PropertyKeySource<?>> propertyKeySources) {
            this.original = original;
            this.propertyKeySources = propertyKeySources;
        }
    
        /**
         * With context.
         *
         * @param sources  the sources
         * @param original the original
         * @return the context.
         */
        public Context with(List<PropertyKeySource<?>> sources, ConfigLoader<Config> original) {
            return new Context(original, sources);
        }
    
        /**
         * With sources context.
         *
         * @param sources the sources
         * @return the context.
         */
        public Context withSources(List<PropertyKeySource<?>> sources) {
            return with(sources, this.original);
        }
    
        /**
         * Gets original.
         *
         * @return the original
         */
        public ConfigLoader<Config> getOriginal() {
            return original;
        }
    
        /**
         * Gets source.
         *
         * @return the source
         */
        public List<PropertyKeySource<?>> getSource() {
            return propertyKeySources;
        }
    }
    
    /**
     * The interface Loader handler.
     *
     * @param <T> the type parameter
     */
    @FunctionalInterface
    interface LoaderHandler<T extends Config> {
        
        /**
         * if done finish this.
         *
         * @param context the context
         * @param config  config.
         */
        void finish(Supplier<Context> context, T config);
    }
}
