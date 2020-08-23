package org.dromara.hmily.config.apollo;

import lombok.Data;
import org.dromara.hmily.config.api.AbstractConfig;
import org.dromara.hmily.config.api.constant.PrefixConstants;
import org.dromara.hmily.spi.HmilySPI;

/**
 * Author:   lilang
 * Date:     2020-08-22 14:53
 * Description:
 **/
@Data
@HmilySPI("remoteApollo")
public class ApolloConfig extends AbstractConfig {

    private String appId;

    private String namespace;

    private String configService;

    private String fileExtension;

    private String secret;

    private String meta;

    private String env;

    @Override
    public String prefix() {
        return PrefixConstants.REMOTE_APOLLO;
    }

}
