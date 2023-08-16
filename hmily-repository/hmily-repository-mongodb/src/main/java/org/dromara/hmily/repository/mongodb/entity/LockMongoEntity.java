package org.dromara.hmily.repository.mongodb.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


/**
 * mongo entity.
 *
 * @author gcedar
 */
@Data
@ToString
@Document(collection = "hmily_lock_global")
public class LockMongoEntity {

    @Field("lock_id")
    @Indexed
    private String lockId;

    @Field("trans_id")
    private Long transId;

    @Field("participant_id")
    private Long participantId;

    @Field("resource_id")
    private String resourceId;

    @Field("target_table_Name")
    private String targetTableName;

    @Field("target_table_pk")
    private String targetTablePk;
}
