/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * tcc分布式事务框架注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Tcc {

    PropagationEnum propagation() default PropagationEnum.PROPAGATION_REQUIRED;

    /**
     * tcc框架确认方法 tcc中第一个c
     *
     * @return confirm方法名称
     */
    String confirmMethod() default "";

    /**
     * tcc框架确认方法 tcc中第二个c
     *
     * @return cancel方法名称
     */
    String cancelMethod() default "";

    /**
     * 模式 tcc 和cc模式
     * <p>
     * tcc模式代表try中有数据库操作，try需要回滚
     * cc模式代表try中无数据库操作，try不需要回滚
     *
     * @return
     */
    TccPatternEnum pattern() default TccPatternEnum.TCC;


}