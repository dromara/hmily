/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.config.loader.bind;

import java.util.function.Supplier;
import lombok.Data;

/**
 * BindData .
 * 2019-08-15 22:12
 *
 * @param <T> the type parameter
 * @author chenbin sixh
 */
@Data
public final class BindData<T> {

    private DataType type;

    private DataType boxType;

    private Supplier<T> value;

    private BindData(final DataType type, final Supplier<T> value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Of bind data.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the bind data
     */
    public static <T> BindData<T> of(final DataType type) {
        return new BindData<>(type, null);
    }

    /**
     * Of bind data.
     *
     * @param <T>   the type parameter
     * @param type  the type
     * @param value the value
     * @return the bind data
     */
    public static <T> BindData<T> of(final DataType type, final Supplier<T> value) {
        return new BindData<>(type, value);
    }

    /**
     * With value bind data.
     *
     * @param <T>   the type parameter
     * @param value the value
     * @return the bind data
     */
    public <T> BindData<T> withValue(final Supplier<T> value) {
        return new BindData<>(this.type, value);
    }
}
