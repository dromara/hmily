package org.dromara.hmily.config.consul;

import lombok.Data;
import org.dromara.hmily.config.api.AbstractConfig;
import org.dromara.hmily.config.api.constant.PrefixConstants;
import org.dromara.hmily.spi.HmilySPI;

/**
 * consul config.
 * @author lilang
 **/
@Data
@HmilySPI("remoteConsul")
public class ConsulConfig extends AbstractConfig {

    private String hostAndPort;

    private String hostAndPorts;

    private String key;

    private long blacklistTimeInMillis = 3000;

    private String fileExtension;

    private boolean update;

    private String updateFileName;

    @Override
    public String prefix() {
        return PrefixConstants.REMOTE_CONSUL;
    }

}
