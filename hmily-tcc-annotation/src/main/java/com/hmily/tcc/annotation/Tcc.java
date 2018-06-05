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

package com.hmily.tcc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * tcc分布式事务框架注解.
 * @author xiaoyu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Tcc {

    /**
     * spring事务传播.
     * @return {@linkplain PropagationEnum}
     */
    PropagationEnum propagation() default PropagationEnum.PROPAGATION_REQUIRED;

    /**
     * tcc框架确认方法 tcc中第一个c.
     *
     * @return confirm方法名称
     */
    String confirmMethod() default "";

    /**
     * tcc框架确认方法 tcc中第二个c.
     *
     * @return cancel方法名称
     */
    String cancelMethod() default "";

    /**
     * 模式 tcc 和cc模式.
     * tcc模式代表try中有数据库操作，try需要回滚.
     * cc模式代表try中无数据库操作，try不需要回滚.
     *
     * @return {@linkplain TccPatternEnum}
     */
    TccPatternEnum pattern() default TccPatternEnum.TCC;

}