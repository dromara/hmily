package org.dromara.hmily.config.etcd;

import lombok.Data;
import org.dromara.hmily.config.api.AbstractConfig;
import org.dromara.hmily.config.api.constant.PrefixConstants;
import org.dromara.hmily.spi.HmilySPI;

/**
 * etcd config.
 *
 * @author lilang
 **/
@Data
@HmilySPI("remoteEtcd")
public class EtcdConfig extends AbstractConfig {

    private String server;

    private String key;

    private long timeoutMs;

    private String fileExtension;

    @Override
    public String prefix() {
        return PrefixConstants.REMOTE_ETCD;
    }
}
