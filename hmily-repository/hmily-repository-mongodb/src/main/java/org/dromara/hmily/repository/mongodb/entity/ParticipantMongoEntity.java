package org.dromara.hmily.repository.mongodb.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * mongo entity.
 *
 * @author gcedar
 */
@Data
@ToString
@Document(collection = "hmily_transaction_participant")
public class ParticipantMongoEntity {

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

}
