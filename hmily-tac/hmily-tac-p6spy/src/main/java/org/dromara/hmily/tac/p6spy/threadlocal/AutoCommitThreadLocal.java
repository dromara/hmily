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

package org.dromara.hmily.tac.p6spy.threadlocal;


/**
 * The enum Auto commit thread local.
 *
 * @author xiaoyu
 */
public enum AutoCommitThreadLocal {
    
    /**
     * Instance auto commit thread local.
     */
    INSTANCE;
    
    private static final ThreadLocal<Boolean> CURRENT_LOCAL = new ThreadLocal<>();
    
    /**
     * Set.
     *
     * @param oldAutoCommit the old auto commit
     */
    public void set(final boolean oldAutoCommit) {
        CURRENT_LOCAL.set(oldAutoCommit);
    }
    
    /**
     * Get boolean.
     *
     * @return the boolean
     */
    public boolean get() {
        return CURRENT_LOCAL.get();
    }
    
    /**
     * Remove.
     */
    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
