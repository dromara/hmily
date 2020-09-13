package org.dromara.hmily.config.apollo;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import org.dromara.hmily.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * The type apollo client.
 *
 * @author lilang
 **/
public class ApolloClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloClient.class);

    private static final String APOLLO_CONFIG_SERVER_ADDR_KEY = "apollo.configService";

    private static final String APOLLO_CONFIG_APPID_KEY = "app.id";

    private static final String APOLLO_CONFIG_META_KEY = "apollo.meta";

    private static final String APOLLO_CONFIG_SECRET_KEY = "apollo.accesskey.secret";

    private static final String APOLLO_CONFIG_ENV_KEY = "env";

    /**
     * Pull input stream.
     *
     * @param config the config
     * @return the input stream
     */
    public InputStream pull(final ApolloConfig config) {
        setApolloConfig(config);
        ConfigFile configFile = ConfigService.getConfigFile(config.getNamespace(), ConfigFileFormat.fromString(config.getFileExtension()));
        String content = configFile.getContent();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("apollo content {}", content);
        }
        if (StringUtils.isBlank(content)) {
            return null;
        }
        return new ByteArrayInputStream(content.getBytes());
    }

    private void setApolloConfig(final ApolloConfig config) {
        System.setProperty(APOLLO_CONFIG_APPID_KEY, config.getAppId());
        if (StringUtils.isNoneBlank(config.getConfigService())) {
            System.setProperty(APOLLO_CONFIG_SERVER_ADDR_KEY, config.getConfigService());
        }
        if (StringUtils.isNoneBlank(config.getMeta())) {
            System.setProperty(APOLLO_CONFIG_META_KEY, config.getMeta());
        }
        if (StringUtils.isNoneBlank(config.getSecret())) {
            System.setProperty(APOLLO_CONFIG_SECRET_KEY, config.getSecret());
        }
        if (StringUtils.isNoneBlank(config.getEnv())) {
            System.setProperty(APOLLO_CONFIG_ENV_KEY, config.getEnv());
        }
    }

}
