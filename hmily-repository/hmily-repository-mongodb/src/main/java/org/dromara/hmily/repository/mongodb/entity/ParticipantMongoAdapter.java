package org.dromara.hmily.repository.mongodb.entity;

import lombok.Data;
import lombok.ToString;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Objects;

@Data
@ToString
@Document(collection="hmily_transaction_participant")
public class ParticipantMongoAdapter {
    @Field("trans_id")
    @Indexed
    private Long transId;
    @Field("app_name")
    private String appName;
    @Field("trans_type")
    private String transType;
    @Field("status")
    private Integer status;
    @Field("retry")
    private Integer retry;
    @Field("version")
    private Integer version;
    @Field("create_time")
    private Date createTime;
    @Field("update_time")
    private Date updateTime;


    @Field("participant_id")
    private Long participantId;
    @Field("participant_ref_id")
    private Long participantRefId;
    @Field("target_class")
    private String targetClass;
    @Field("target_method")
    private String targetMethod;
    @Field("confirm_method")
    private String confirmMethod;
    @Field("cancel_method")
    private String cancelMethod;
    @Field("role")
    private Integer role;
    @Field("confirm_invocation")
    private byte[] confirmInvocation;
    @Field("cancel_invocation")
    private byte[] cancelInvocation;


    public HmilyParticipant convert(HmilySerializer hmilySerializer) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId(getParticipantId());
        hmilyParticipant.setParticipantRefId(getParticipantRefId());
        hmilyParticipant.setTransId(getTransId());
        hmilyParticipant.setTransType(getTransType());
        hmilyParticipant.setStatus(getStatus());
        hmilyParticipant.setRole(getRole());
        hmilyParticipant.setRetry(getRetry());
        hmilyParticipant.setAppName(getAppName());
        hmilyParticipant.setTargetClass(getTargetClass());
        hmilyParticipant.setTargetMethod(getTargetMethod());
        hmilyParticipant.setConfirmMethod(getConfirmMethod());
        hmilyParticipant.setCancelMethod(getCancelMethod());
        try {
            if (Objects.nonNull(getConfirmInvocation())) {
                byte[] confirmInvocation = getConfirmInvocation();
                final HmilyInvocation confirmHmilyInvocation =
                        hmilySerializer.deSerialize(confirmInvocation, HmilyInvocation.class);
                hmilyParticipant.setConfirmHmilyInvocation(confirmHmilyInvocation);
            }
            if (Objects.nonNull(getCancelInvocation())) {
                byte[] cancelInvocation = getCancelInvocation();
                final HmilyInvocation cancelHmilyInvocation =
                        hmilySerializer.deSerialize(cancelInvocation, HmilyInvocation.class);
                hmilyParticipant.setCancelHmilyInvocation(cancelHmilyInvocation);
            }
        } catch (HmilySerializerException e) {
        }
        hmilyParticipant.setVersion(getVersion());
        return hmilyParticipant;
    }
    public static ParticipantMongoAdapter create(HmilyParticipant hmilyParticipant, HmilySerializer hmilySerializer, String appName){
        byte[] confirmSerialize = null;
        byte[] cancelSerialize = null;
        if (Objects.nonNull(hmilyParticipant.getConfirmHmilyInvocation())) {
            confirmSerialize = hmilySerializer.serialize(hmilyParticipant.getConfirmHmilyInvocation());
        }
        if (Objects.nonNull(hmilyParticipant.getCancelHmilyInvocation())) {
            cancelSerialize = hmilySerializer.serialize(hmilyParticipant.getCancelHmilyInvocation());
        }
        ParticipantMongoAdapter entity = new ParticipantMongoAdapter();
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
}
