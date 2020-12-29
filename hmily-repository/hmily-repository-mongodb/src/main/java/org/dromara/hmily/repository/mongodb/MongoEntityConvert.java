package org.dromara.hmily.repository.mongodb;

import org.dromara.hmily.repository.mongodb.entity.ParticipantMongoEntity;
import org.dromara.hmily.repository.mongodb.entity.TransactionMongoEntity;
import org.dromara.hmily.repository.mongodb.entity.UndoMongoEntity;
import org.dromara.hmily.repository.spi.entity.HmilyDataSnapshot;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * mongo entity convert.
 *
 * @author gcedar
 */
public class MongoEntityConvert {
    private static Logger logger = LoggerFactory.getLogger(MongoEntityConvert.class);

    private HmilySerializer hmilySerializer;

    public MongoEntityConvert(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }

    /**
     * 转换mongo对象.
     * @param mongoEntity mongoEntity.
     * @return entity.
     */
    public HmilyParticipant convert(final ParticipantMongoEntity mongoEntity) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(mongoEntity.getParticipantId());
        hmilyParticipant.setParticipantRefId(mongoEntity.getParticipantRefId());
        hmilyParticipant.setTransId(mongoEntity.getTransId());
        hmilyParticipant.setTransType(mongoEntity.getTransType());
        hmilyParticipant.setStatus(mongoEntity.getStatus());
        hmilyParticipant.setRole(mongoEntity.getRole());
        hmilyParticipant.setRetry(mongoEntity.getRetry());
        hmilyParticipant.setAppName(mongoEntity.getAppName());
        hmilyParticipant.setTargetClass(mongoEntity.getTargetClass());
        hmilyParticipant.setTargetMethod(mongoEntity.getTargetMethod());
        hmilyParticipant.setConfirmMethod(mongoEntity.getConfirmMethod());
        hmilyParticipant.setCancelMethod(mongoEntity.getCancelMethod());
        try {
            if (Objects.nonNull(mongoEntity.getConfirmInvocation())) {
                byte[] confirmInvocation = mongoEntity.getConfirmInvocation();
                final HmilyInvocation confirmHmilyInvocation =
                        hmilySerializer.deSerialize(confirmInvocation, HmilyInvocation.class);
                hmilyParticipant.setConfirmHmilyInvocation(confirmHmilyInvocation);
            }
            if (Objects.nonNull(mongoEntity.getCancelInvocation())) {
                byte[] cancelInvocation = mongoEntity.getCancelInvocation();
                final HmilyInvocation cancelHmilyInvocation =
                        hmilySerializer.deSerialize(cancelInvocation, HmilyInvocation.class);
                hmilyParticipant.setCancelHmilyInvocation(cancelHmilyInvocation);
            }
        } catch (HmilySerializerException e) {
            logger.error("mongo 存储序列化错误", e);
        }
        hmilyParticipant.setVersion(mongoEntity.getVersion());
        return hmilyParticipant;
    }

    /**
     * 转换mongo对象.
     * @param entity transaction mongo entity.
     * @return entity.
     */
    public HmilyTransaction convert(final TransactionMongoEntity entity) {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId(entity.getTransId());
        hmilyTransaction.setTransType(entity.getTransType());
        hmilyTransaction.setStatus(entity.getStatus());
        hmilyTransaction.setAppName(entity.getAppName());
        hmilyTransaction.setRetry(entity.getRetry());
        hmilyTransaction.setVersion(entity.getVersion());
        return hmilyTransaction;
    }

    /**
     * 转换mongo对象.
     * @param entity mongo entity.
     * @return hmily entity.
     */
    public HmilyParticipantUndo convert(final UndoMongoEntity entity) {
        HmilyParticipantUndo undo = new HmilyParticipantUndo();
        undo.setUndoId(entity.getUndoId());
        undo.setParticipantId(entity.getParticipantId());
        undo.setTransId(entity.getTransId());
        undo.setResourceId(entity.getResourceId());
        byte[] snapshotBytes = entity.getDataSnapshot();
        try {
            HmilyDataSnapshot dataSnapshot =
                    hmilySerializer.deSerialize(snapshotBytes, HmilyDataSnapshot.class);
            undo.setDataSnapshot(dataSnapshot);
        } catch (HmilySerializerException e) {
            logger.error("mongo 存储序列化错误", e);
        }
        undo.setStatus(entity.getStatus());
        return undo;
    }

    /**
     * 转换mongo对象.
     * @param hmilyParticipant hmilyParticipant entity.
     * @param appName app name.
     * @return entity.
     */
    public ParticipantMongoEntity create(final HmilyParticipant hmilyParticipant, final String appName) {
        byte[] confirmSerialize = null;
        byte[] cancelSerialize = null;
        if (Objects.nonNull(hmilyParticipant.getConfirmHmilyInvocation())) {
            confirmSerialize = hmilySerializer.serialize(hmilyParticipant.getConfirmHmilyInvocation());
        }
        if (Objects.nonNull(hmilyParticipant.getCancelHmilyInvocation())) {
            cancelSerialize = hmilySerializer.serialize(hmilyParticipant.getCancelHmilyInvocation());
        }
        ParticipantMongoEntity entity = new ParticipantMongoEntity();
        entity.setAppName(appName);
        entity.setCancelInvocation(cancelSerialize);
        entity.setCancelMethod(hmilyParticipant.getCancelMethod());
        entity.setConfirmInvocation(confirmSerialize);
        entity.setConfirmMethod(hmilyParticipant.getConfirmMethod());
        entity.setCreateTime(hmilyParticipant.getCreateTime());
        entity.setParticipantId(hmilyParticipant.getParticipantId());
        entity.setParticipantRefId(hmilyParticipant.getParticipantRefId());
        entity.setRetry(hmilyParticipant.getRetry());
        entity.setRole(hmilyParticipant.getRole());
        entity.setStatus(hmilyParticipant.getStatus());
        entity.setTargetClass(hmilyParticipant.getTargetClass());
        entity.setTargetMethod(hmilyParticipant.getTargetMethod());
        entity.setTransId(hmilyParticipant.getTransId());
        entity.setTransType(hmilyParticipant.getTransType());
        entity.setUpdateTime(hmilyParticipant.getUpdateTime());
        entity.setVersion(hmilyParticipant.getVersion());
        return entity;
    }

    /**
     * 转换mongo对象.
     * @param hmilyTransaction hmily transaction entity.
     * @param appName app name.
     * @return entity.
     */
    public TransactionMongoEntity create(final HmilyTransaction hmilyTransaction, final String appName) {
        TransactionMongoEntity entity = new TransactionMongoEntity();
        entity.setTransId(hmilyTransaction.getTransId());
        entity.setAppName(appName);
        entity.setStatus(hmilyTransaction.getStatus());
        entity.setTransType(hmilyTransaction.getTransType());
        entity.setRetry(hmilyTransaction.getRetry());
        entity.setVersion(hmilyTransaction.getVersion());
        entity.setCreateTime(hmilyTransaction.getCreateTime());
        entity.setUpdateTime(hmilyTransaction.getUpdateTime());
        return entity;
    }

    /**
     * 转换mongo对象.
     * @param undo hmily entity.
     * @return mongo entity.
     */
    public UndoMongoEntity create(final HmilyParticipantUndo undo) {
        UndoMongoEntity entity = new UndoMongoEntity();
        entity.setCreateTime(undo.getCreateTime());
        entity.setParticipantId(undo.getParticipantId());
        entity.setResourceId(undo.getResourceId());
        entity.setStatus(undo.getStatus());
        entity.setTransId(undo.getTransId());
        entity.setUndoId(undo.getUndoId());
        entity.setDataSnapshot(hmilySerializer.serialize(undo.getDataSnapshot()));
        entity.setUpdateTime(undo.getUpdateTime());
        return entity;
    }
}
