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
@Document(collection = "hmily_participant_undo")
public class UndoMongoEntity {

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

    @Field("data_snapshot")
    private byte[] dataSnapshot;

}
