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

package org.dromara.hmily.tac.p6spy;

import com.p6spy.engine.spy.P6DataSource;
import lombok.Getter;
import org.dromara.hmily.tac.common.HmilyResourceManager;
import org.dromara.hmily.tac.common.HmilyTacResource;
import org.dromara.hmily.tac.common.database.type.DatabaseTypeFactory;
import org.dromara.hmily.tac.common.utils.DatabaseTypes;
import org.dromara.hmily.tac.common.utils.ResourceIdUtils;
import org.dromara.hmily.tac.metadata.HmilyMetaDataManager;
import org.dromara.hmily.tac.p6spy.rollback.HmilyTacRollbackExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The type Hmily p 6 datasource.
 *
 * @author xiaoyu
 */
public class HmilyP6Datasource extends P6DataSource implements HmilyTacResource {
    
    private static final long serialVersionUID = -5117674683387217309L;
    
    @Getter
    private final DataSource targetDataSource;
    
    private String jdbcUrl;
    
    /**
     * Instantiates a new Hmily p 6 datasource.
     *
     * @param delegate the delegate
     */
    public HmilyP6Datasource(final DataSource delegate) {
        super(delegate);
        targetDataSource = delegate;
        init();
    }
    
    private void init() {
        try (Connection connection = targetDataSource.getConnection()) {
            jdbcUrl = connection.getMetaData().getURL();
            DatabaseTypes.INSTANCE.setDatabaseType(DatabaseTypeFactory.getDatabaseTypeByURL(jdbcUrl));
        } catch (SQLException e) {
            throw new IllegalStateException("can not init dataSource", e);
        }
        HmilyMetaDataManager.register(this, DatabaseTypes.INSTANCE.getDatabaseType());
        HmilyResourceManager.register(this);
        HmilyTacRollbackExecutor.getInstance();
    }
    
    @Override
    public String getResourceId() {
        return ResourceIdUtils.INSTANCE.getResourceId(jdbcUrl);
    }
    
}
