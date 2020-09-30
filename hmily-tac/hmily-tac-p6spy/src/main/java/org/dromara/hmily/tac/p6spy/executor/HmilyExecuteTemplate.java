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

package org.dromara.hmily.tac.p6spy.executor;

import com.p6spy.engine.common.StatementInformation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.TransTypeEnum;
import org.dromara.hmily.common.enums.HmilyActionEnum;
import org.dromara.hmily.common.utils.IdWorkerUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.dromara.hmily.core.repository.HmilyRepositoryStorage;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.tac.common.utils.DatabaseTypes;
import org.dromara.hmily.tac.common.utils.ResourceIdUtils;
import org.dromara.hmily.tac.core.cache.HmilyParticipantUndoCacheManager;
import org.dromara.hmily.tac.core.cache.HmilyUndoContextCacheManager;
import org.dromara.hmily.tac.core.context.HmilyUndoContext;
import org.dromara.hmily.tac.p6spy.threadlocal.AutoCommitThreadLocal;
import org.dromara.hmily.tac.sqlcompute.HmilySQLComputeEngine;
import org.dromara.hmily.tac.sqlcompute.HmilySQLComputeEngineFactory;
import org.dromara.hmily.tac.sqlparser.model.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.spi.HmilySqlParserEngine;
import org.dromara.hmily.tac.sqlparser.spi.HmilySqlParserEngineFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The enum Hmily execute template.
 */
@Slf4j
public enum HmilyExecuteTemplate {
    
    /**
     * Instance hmily execute template.
     */
    INSTANCE;
    
    /**
     * Sets auto commit.
     *
     * @param connection the connection
     */
    public void beforeSetAutoCommit(final Connection connection) {
        if (check()) {
            return;
        }
        try {
            boolean autoCommit = connection.getAutoCommit();
            if (autoCommit) {
                connection.setAutoCommit(false);
            }
            AutoCommitThreadLocal.INSTANCE.set(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Execute.
     *
     * @param statementInformation the statement information
     */
    public void execute(final StatementInformation statementInformation) {
        if (check()) {
            return;
        }
        try {
            HmilySqlParserEngine hmilySqlParserEngine = HmilySqlParserEngineFactory.newInstance();
            // TODO prepared sql will improve performance of parser engine
            String sql = statementInformation.getSqlWithValues();
            HmilyStatement statement = hmilySqlParserEngine.parser(sql, DatabaseTypes.INSTANCE.getDatabaseType());
            // TODO should generate lock-key to avoid dirty data modified by other global transaction.
            HmilySQLComputeEngine hmilySQLComputeEngine = HmilySQLComputeEngineFactory.newInstance(statement);
            HmilyUndoInvocation hmilyUndoInvocation = hmilySQLComputeEngine.generateImage(statementInformation.getConnectionInformation().getConnection(), sql);
            //4.缓存sql日志记录 ? 存储到哪里呢 threadLocal？
            HmilyUndoContext context = new HmilyUndoContext();
            context.setUndoInvocation(hmilyUndoInvocation);
            context.setResourceId(ResourceIdUtils.INSTANCE.getResourceId(statementInformation.getConnectionInformation().getUrl()));
            HmilyTransactionContext transactionContext = HmilyContextHolder.get();
            context.setTransId(transactionContext.getTransId());
            context.setParticipantId(transactionContext.getParticipantId());
            HmilyUndoContextCacheManager.INSTANCE.set(context);
        } catch (Exception e) {
            log.error("execute hmily tac module have exception:", e);
        }
    }
    
    /**
     * Commit.
     *
     * @param connection the connection
     */
    public void commit(final Connection connection) {
        if (check()) {
            return;
        }
        List<HmilyParticipantUndo> undoList = buildUndoList();
        for (HmilyParticipantUndo undo : undoList) {
            //缓存
            HmilyParticipantUndoCacheManager.getInstance().cacheHmilyParticipantUndo(undo);
            //存储
            HmilyRepositoryStorage.createHmilyParticipantUndo(undo);
        }
        //清除
        clean(connection);
    }
    
    /**
     * clean.
     *
     * @param connection the connection
     */
    @SneakyThrows
    public void clean(final Connection connection) {
        if (check()) {
            return;
        }
        connection.setAutoCommit(AutoCommitThreadLocal.INSTANCE.get());
        HmilyUndoContextCacheManager.INSTANCE.remove();
        AutoCommitThreadLocal.INSTANCE.remove();
    }
    
    private List<HmilyParticipantUndo> buildUndoList() {
        List<HmilyUndoContext> contexts = HmilyUndoContextCacheManager.INSTANCE.get();
        return contexts.stream().map(context -> {
            HmilyParticipantUndo undo = new HmilyParticipantUndo();
            undo.setResourceId(context.getResourceId());
            undo.setUndoId(IdWorkerUtils.getInstance().createUUID());
            undo.setParticipantId(context.getParticipantId());
            undo.setTransId(context.getTransId());
            undo.setUndoInvocation(context.getUndoInvocation());
            undo.setStatus(HmilyActionEnum.TRYING.getCode());
            return undo;
        }).collect(Collectors.toList());
        
    }
    
    private boolean check() {
        HmilyTransactionContext transactionContext = HmilyContextHolder.get();
        return Objects.isNull(transactionContext) || !TransTypeEnum.TAC.name().equalsIgnoreCase(transactionContext.getTransType());
    }
}
