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
@Document(collection = "hmily_transaction_global")
public class TransactionMongoEntity {

    @Field("trans_id")
    @Indexed
    private Long transId;

    @Field("app_name")
    private String appName;

    @Field("status")
    private Integer status;

    @Field("trans_type")
    private String transType;

    @Field("retry")
    private Integer retry;

    @Field("version")
    private Integer version;

    @Field("create_time")
    private Date createTime;

    @Field("update_time")
    private Date updateTime;

}
