package org.dromara.hmily.config.api.entity;

import lombok.Data;
import org.dromara.hmily.config.api.AbstractConfig;
import org.dromara.hmily.config.api.constant.PrefixConstants;
import org.dromara.hmily.spi.HmilySPI;

/**
 * The etcd config.
 *
 * @author dongzl
 */
@Data
@HmilySPI("hmilyEtcdConfig")
public class HmilyEtcdConfig extends AbstractConfig {

    private String host;

    private String rootPath = "/hmily";

    @Override
    public String prefix() {
        return PrefixConstants.ETCD_PREFIX;
    }
}
