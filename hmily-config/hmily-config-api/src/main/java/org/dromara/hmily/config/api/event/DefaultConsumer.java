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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom monitor all changes.
 *
 * @author sixh chenbin
 */
public class DefaultConsumer implements EventConsumer<EventData> {

    private final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);

    @Override
    public void accept(final EventData data) {
        logger.info("{}:config has changed....", data);
    }

    @Override
    public String regex() {
        return "[\\s\\S\\d\\D]*";
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }
}
