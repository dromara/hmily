package org.dromara.hmily.repository.mongodb.entity;

import lombok.Data;
import lombok.ToString;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@ToString
@Document(collection="hmily_transaction_global")
public class TransactionMongoAdapter {
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
    public HmilyTransaction convert() {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId(getTransId());
        hmilyTransaction.setTransType(getTransType());
        hmilyTransaction.setStatus(getStatus());
        hmilyTransaction.setAppName(getAppName());
        hmilyTransaction.setRetry(getRetry());
        hmilyTransaction.setVersion(getVersion());
        return hmilyTransaction;
    }
    public static TransactionMongoAdapter create(HmilyTransaction hmilyTransaction, String appName) {
        TransactionMongoAdapter entity = new TransactionMongoAdapter();
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
}
