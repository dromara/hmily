/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.config.api.event;

import org.dromara.hmily.config.api.Config;

/**
 * EventData .
 * Changed event data definition
 *
 * @author sixh chenbin
 */
public class EventData {

    private Config config;

    /**
     * Changed value.
     */
    private Object value;

    /**
     * Changed key.
     */
    private String properties;

    /**
     * subscription processing.
     */
    private String subscribe;

    private final ChangeEvent event;

    public EventData(ChangeEvent event, String properties, Object value) {
        this.value = value;
        this.properties = properties;
        this.event = event;
    }

    /**
     * Gets config.
     *
     * @param <M> the type parameter
     * @return the config
     */
    public <M extends Config> M getConfig() {
        return (M) config;
    }

    /**
     * Sets config.
     *
     * @param config the config
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(String subscribe) {
        this.subscribe = subscribe;
    }

    public ChangeEvent getEvent() {
        return event;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
