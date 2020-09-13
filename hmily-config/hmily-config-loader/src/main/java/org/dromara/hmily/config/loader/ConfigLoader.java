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

import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.event.EventConsumer;
import org.dromara.hmily.config.api.event.EventData;
import org.dromara.hmily.config.loader.bind.BindData;
import org.dromara.hmily.config.loader.bind.Binder;
import org.dromara.hmily.config.loader.bind.DataType;
import org.dromara.hmily.config.loader.property.ConfigPropertySource;
import org.dromara.hmily.config.loader.property.DefaultConfigPropertySource;
import org.dromara.hmily.config.loader.property.MapPropertyKeySource;
import org.dromara.hmily.config.loader.property.PropertyKeyParse;
import org.dromara.hmily.config.loader.property.PropertyKeySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ConfigLoader.
 *
 * @param <T> the type parameter
 * @author xiaoyu
 * @author chenbin sixh
 */
public interface ConfigLoader<T extends Config> {

    /**
     * The constant log.
     */
    Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);

    /**
     * Load related configuration information.
     *
     * @param context the context
     * @param handler the handler
     */
    void load(Supplier<Context> context, LoaderHandler<T> handler);

    /**
     * Implementation of Active Remote Push.
     *
     * @param context the context
     * @param data    the data
     */
    default void push(final Supplier<Context> context,
                      final EventData data) {
        if (data == null) {
            return;
        }
        Set<EventConsumer<EventData>> events = ConfigEnv.getInstance().getEvents();
        if (events.isEmpty()) {
            return;
        }
        String properties = data.getProperties();
        List<EventConsumer<EventData>> eventsLists = events.stream()
                .filter(e -> !Objects.isNull(e.properties()))
                .filter(e -> Pattern.matches(e.properties(), properties))
                .collect(Collectors.toList());
        for (EventConsumer<EventData> consumer : eventsLists) {
            Optional<Config> first = ConfigEnv.getInstance().stream().filter(e -> properties.startsWith(e.prefix())).findFirst();
            first.ifPresent(x -> {
                List<PropertyKeySource<?>> sources = new ArrayList<>();
                Map<String, Object> values = new HashMap<>(1);
                values.put(properties, data.getValue());
                sources.add(new MapPropertyKeySource(first.get().prefix(), values));
                PassiveHandler<Config> handler = (ct, cf) -> {
                    data.setConfig(cf);
                    data.setSubscribe(consumer.properties());
                    try {
                        consumer.accept(data);
                    } catch (ClassCastException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("EventData of type [{}] not accepted by EventConsumer [{}]", data.getClass(), consumer);
                        }
                    }
                };
                context.get().getOriginal().passive(() -> context.get().withSources(sources), handler, first.get());
            });
        }
    }

    /**
     * Passive subscription processes related events. When the current event is processed,
     * the push method is called to push it to subscribers in the system.
     *
     * @param context the context
     * @param handler the handler
     * @param config  Configuration information of things processed by load method
     * @see #push(Supplier, EventData) #push(Supplier, EventData)
     */
    default void passive(final Supplier<Context> context,
                         final PassiveHandler<Config> handler,
                         Config config) {
    }

    /**
     * Again load.
     *
     * @param context the context
     * @param handler the handler
     * @param tClass  the t class
     */
    default void againLoad(final Supplier<Context> context, final LoaderHandler<T> handler, final Class<T> tClass) {
        T config = ConfigEnv.getInstance().getConfig(tClass);
        for (PropertyKeySource<?> propertyKeySource : context.get().getSource()) {
            ConfigPropertySource configPropertySource = new DefaultConfigPropertySource<>(propertyKeySource, PropertyKeyParse.INSTANCE);
            Binder binder = Binder.of(configPropertySource);
            T newConfig = binder.bind(config.prefix(), BindData.of(DataType.of(tClass), () -> config));
            handler.finish(context, newConfig);
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
        public Context(final List<PropertyKeySource<?>> propertyKeySources) {
            this(null, propertyKeySources);
        }

        /**
         * Instantiates a new Context.
         *
         * @param original           the original
         * @param propertyKeySources the property key sources
         */
        public Context(final ConfigLoader<Config> original, final List<PropertyKeySource<?>> propertyKeySources) {
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
        public Context with(final List<PropertyKeySource<?>> sources, final ConfigLoader<Config> original) {
            return new Context(original, sources);
        }

        /**
         * With sources context.
         *
         * @param sources the sources
         * @return the context.
         */
        public Context withSources(final List<PropertyKeySource<?>> sources) {
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

    /**
     * The interface Passive handler.
     *
     * @param <T> the type parameter
     */
    @FunctionalInterface
    interface PassiveHandler<T extends Config> {

        /**
         * if done finish this.
         *
         * @param context the context
         * @param config  the config
         */
        void passive(Supplier<Context> context, T config);
    }
}
