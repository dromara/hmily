package org.dromara.hmily.config.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * consul client.
 * @author lilang
 **/
public final class ConsulClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulClient.class);

    private Consul consul;

    private ConsulClient() {

    }

    /**
     * set consul.
     * @param consul consul client
     */
    public void setConsul(final Consul consul) {
        this.consul = consul;
    }

    /**
     * get instance.
     * @param consulConfig consul config
     * @return consulClient
     */
    public static ConsulClient getInstance(final ConsulConfig consulConfig) {
        String hostAndPorts = consulConfig.getHostAndPorts();
        List<HostAndPort> hostAndPortList = buildHostAndPortList(hostAndPorts);
        Consul consul;
        if (StringUtils.isNoneBlank(consulConfig.getHostAndPorts())) {
            consul = Consul.builder().withMultipleHostAndPort(hostAndPortList, consulConfig.getBlacklistTimeInMillis()).build().newClient();
        } else {
            consul = Consul.builder().withHostAndPort(HostAndPort.fromString(consulConfig.getHostAndPort())).build().newClient();
        }

        ConsulClient consulClient = new ConsulClient();
        consulClient.setConsul(consul);
        return consulClient;
    }

    /**
     * build hostAndPorts.
     * @param hostAndPorts address
     * @return HostAndPortList
     */
    private static List<HostAndPort> buildHostAndPortList(final String hostAndPorts) {
        if (StringUtils.isNoneBlank(hostAndPorts)) {
            String[] hostAndPortArray = hostAndPorts.split(",");
            List<HostAndPort> hostAndPortList = new ArrayList<>();
            for (String hostAndPort : hostAndPortArray) {
                hostAndPortList.add(HostAndPort.fromString(hostAndPort));
            }
            return hostAndPortList;
        }
        return Collections.emptyList();
    }

    /**
     * pull.
     * @param consulConfig consul config
     * @return InputStream
     */
    public InputStream pull(final ConsulConfig consulConfig) {
        if (consul == null) {
            if (StringUtils.isNoneBlank(consulConfig.getHostAndPorts())) {
                consul = Consul.builder().withMultipleHostAndPort(buildHostAndPortList(consulConfig.getHostAndPorts()), consulConfig.getBlacklistTimeInMillis()).build().newClient();
            } else {
                consul = Consul.builder().withHostAndPort(HostAndPort.fromString(consulConfig.getHostAndPort())).build().newClient();
            }
        }

        Value value = consul.keyValueClient().getValue(consulConfig.getKey()).orElse(null);
        if (value == null) {
            return null;
        }

        String content = value.getValueAsString(Charset.forName("utf-8")).get();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("consul content {}", content);
        }
        if (StringUtils.isBlank(content)) {
            return null;
        }
        return new ByteArrayInputStream(content.getBytes(Charset.forName("utf-8")));
    }

    /**
     * put config content.
     *
     * @param key config key
     * @param content config content
     */
    public void put(final String key, final String content) {
        consul.keyValueClient().putValue(key, content);
    }

    /**
     * Add listener.
     *
     * @param context        the context
     * @param passiveHandler the passive handler
     * @param config         the config
     * @throws InterruptedException exception
     */
    void addListener(final Supplier<ConfigLoader.Context> context, final ConfigLoader.PassiveHandler<ConsulPassiveConfig> passiveHandler, final ConsulConfig config) throws InterruptedException {
        if (!config.isPassive()) {
            return;
        }
        if (consul == null) {
            LOGGER.warn("Consul client is null...");
        }

        ConsulCache consulCache = KVCache.newCache(consul.keyValueClient(), config.getKey());
        consulCache.addListener(map -> {
            Set<Map.Entry<Object, Value>> set = map.entrySet();
            set.forEach(x -> {
                ConsulPassiveConfig consulPassiveConfig = new ConsulPassiveConfig();
                consulPassiveConfig.setKey(config.getKey());
                consulPassiveConfig.setFileExtension(config.getFileExtension());
                consulPassiveConfig.setValue(x.getValue().getValueAsString(Charset.forName("utf-8")).get());
                passiveHandler.passive(context, consulPassiveConfig);
            });
        });
        consulCache.start();
        LOGGER.info("passive consul remote started....");
    }
}
