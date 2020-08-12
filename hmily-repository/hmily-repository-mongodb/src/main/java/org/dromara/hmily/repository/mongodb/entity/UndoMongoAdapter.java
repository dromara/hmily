package org.dromara.hmily.repository.mongodb.entity;

import lombok.Data;
import lombok.ToString;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@ToString
@Document(collection="hmily_participant_undo")
public class UndoMongoAdapter {
    @Field("trans_id")
    private Long transId;
    @Field("status")
    private Integer status;
    @Field("create_time")
    private Date createTime;
    @Field("update_time")
    private Date updateTime;
    @Field("participant_id")
    private Long participantId;

    @Field("undo_id")
    @Indexed
    private Long undoId;
    @Field("resource_id")
    private String resourceId;
    @Field("undo_invocation")
    private byte[] undoInvocation;

    public HmilyParticipantUndo convert(HmilySerializer hmilySerializer) {
        HmilyParticipantUndo undo = new HmilyParticipantUndo();
        undo.setUndoId( getUndoId());
        undo.setParticipantId( getParticipantId());
        undo.setTransId( getTransId());
        undo.setResourceId( getResourceId());
        byte[] undoInvocation =  getUndoInvocation();
        try {
            final HmilyUndoInvocation hmilyUndoInvocation =
                    hmilySerializer.deSerialize(undoInvocation, HmilyUndoInvocation.class);
            undo.setUndoInvocation(hmilyUndoInvocation);
        } catch (HmilySerializerException e) {
        }
        undo.setStatus(getStatus());
        return undo;
    }
    public static UndoMongoAdapter create(HmilyParticipantUndo undo, HmilySerializer hmilySerializer) {
        UndoMongoAdapter entity = new UndoMongoAdapter();
        entity.setCreateTime(undo.getCreateTime());
        entity.setParticipantId(undo.getParticipantId());
        entity.setResourceId(undo.getResourceId());
        entity.setStatus(undo.getStatus());
        entity.setTransId(undo.getTransId());
        entity.setUndoId(undo.getUndoId());
        entity.setUndoInvocation(hmilySerializer.serialize(undo.getUndoInvocation()));
        entity.setUpdateTime(undo.getUpdateTime());
        return entity;
    }
}
