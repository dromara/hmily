package org.dromara.hmily.demo.dubbo.account.ext.log;

import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;

import java.util.Date;
import java.util.List;

/**
 * @author xiaoyu(Myth)
 */
@HmilySPI("custom")
public class CustomCoordinatorRepository implements HmilyCoordinatorRepository {

    private ObjectSerializer serializer;

    @Override
    public int create(HmilyTransaction hmilyTransaction) {
        return 0;
    }

    @Override
    public int remove(String id) {
        return 0;
    }

    @Override
    public int update(HmilyTransaction hmilyTransaction) {
        return 0;
    }

    @Override
    public int updateParticipant(HmilyTransaction hmilyTransaction) {
        return 0;
    }

    @Override
    public int updateStatus(String id, Integer status) {
        return 0;
    }

    @Override
    public HmilyTransaction findById(String id) {
        return null;
    }

    @Override
    public List<HmilyTransaction> listAll() {
        return null;
    }

    @Override
    public List<HmilyTransaction> listAllByDelay(Date date) {
        return null;
    }

    @Override
    public void init(String modelName, HmilyConfig hmilyConfig) {
        System.out.println("executor customer CustomCoordinatorRepository");
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public void setSerializer(ObjectSerializer objectSerializer) {
        this.serializer = objectSerializer;
    }
}
