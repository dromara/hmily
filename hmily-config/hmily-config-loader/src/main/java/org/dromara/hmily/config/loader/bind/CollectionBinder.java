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

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.config.loader.property.PropertyName;

/**
 * The type Collection binder.
 *
 * @author xiaoyu
 */
public final class CollectionBinder extends IndexedBinder<Collection<Object>> {
    
    /**
     * Instantiates a new Collection binder.
     *
     * @param env the env
     */
    public CollectionBinder(final Binder.Env env) {
        super(env);
    }

    @Override
    public Object bind(final PropertyName propertyName, final BindData<?> target, final Binder.Env env, final AggregateElementBinder elementBinder) {
        DataType type = target.getType();
        Class<?> collectionType = target.getValue() != null ? List.class : type.getTypeClass();
        DataType aggregateType = DataType.of(collectionType);
        DataType elementType = type.getGenerics().length > 0 ? type.getGenerics()[0] : DataType.of(Object.class);
        IndexedBinder.IndexedCollectionSupplier result = new IndexedBinder.IndexedCollectionSupplier(() -> CollectionUtils.createFactory().create(collectionType, 0));
        bindIndexed(propertyName, target, elementBinder, aggregateType, elementType, result);
        if (result.wasSupplied()) {
            return result.get();
        }
        return null;
    }

    @Override
    public Object merge(final Supplier<?> targetValue, final Collection<Object> object) {
        Collection<Object> existingCollection = getExistingIfPossible(targetValue);
        if (existingCollection == null) {
            return object;
        }
        try {
            existingCollection.clear();
            existingCollection.addAll(object);
            return copyIfPossible(existingCollection);
        } catch (UnsupportedOperationException ex) {
            return createNewCollection(object);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> getExistingIfPossible(final Supplier<?> existing) {
        try {
            return (Collection<Object>) existing.get();
        } catch (Exception ex) {
            return null;
        }
    }

    private Collection<Object> copyIfPossible(final Collection<Object> collection) {
        try {
            return createNewCollection(collection);
        } catch (Exception ex) {
            return collection;
        }
    }

    private Collection<Object> createNewCollection(final Collection<Object> collection) {
        Collection<Object> result = CollectionUtils.createFactory().create(collection.getClass(), collection.size());
        result.addAll(collection);
        return result;
    }
}
