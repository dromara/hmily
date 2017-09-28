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
package com.happylifeplat.tcc.core.bean.entity;

import java.io.Serializable;
import java.util.Arrays;


public class TccInvocation implements Serializable {

    private static final long serialVersionUID = -5108578223428529356L;
    private Class targetClass;

    private String methodName;

    private Class[] parameterTypes;

    private Object[] args;

    public TccInvocation() {

    }

    public TccInvocation(Class targetClass, String methodName, Class[] parameterTypes, Object... args) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.targetClass = targetClass;
        this.args = args;
    }

    public Class getTargetClass() {
        return targetClass;
    }


    public String getMethodName() {
        return methodName;
    }


    public Class[] getParameterTypes() {
        return parameterTypes;
    }


    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "TccInvocation{" +
                "targetClass=" + targetClass +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
