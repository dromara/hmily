package org.dromara.hmily.config.consul;

import org.dromara.hmily.common.utils.FileUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.event.AddData;
import org.dromara.hmily.config.api.event.EventData;
import org.dromara.hmily.config.api.event.ModifyData;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.PropertyLoader;
import org.dromara.hmily.config.loader.properties.PropertiesLoader;
import org.dromara.hmily.config.loader.yaml.YamlPropertyLoader;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * consul config loader.
 * @author lilang
 **/
@HmilySPI("consul")
public class ConsulConfigLoader implements ConfigLoader<ConsulConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulConfigLoader.class);

    private static final Map<String, PropertyLoader> LOADERS = new HashMap<>();

    private ConsulClient client;

    static {
        LOADERS.put("yml", new YamlPropertyLoader());
        LOADERS.put("properties", new PropertiesLoader());
    }

    /**
     * Instantiates a new Consul config loader.
     */
    public ConsulConfigLoader() {
    }

    /**
     * Instantiates a new Consul config loader.
     *
     * @param client the client
     */
    public ConsulConfigLoader(final ConsulClient client) {
        this();
        this.client = client;
    }

    @Override
    public void load(final Supplier<ConfigLoader.Context> context, final ConfigLoader.LoaderHandler<ConsulConfig> handler) {
        ConfigLoader.LoaderHandler<ConsulConfig> consulHandler = (c, config) -> consulLoad(c, handler, config);
        againLoad(context, consulHandler, ConsulConfig.class);
    }

    private void consulLoad(final Supplier<ConfigLoader.Context> context, final ConfigLoader.LoaderHandler<ConsulConfig> handler, final ConsulConfig config) {
        if (config != null) {
            check(config);
            if (Objects.isNull(client)) {
                client = ConsulClient.getInstance(config);
            }
            if (config.isUpdate()) {
                client.put(config.getKey(), FileUtils.readYAML(config.getUpdateFileName()));
            }
            LOGGER.info("loader consul config: {}", config);
            String fileExtension = config.getFileExtension();
            PropertyLoader propertyLoader = LOADERS.get(fileExtension);
            if (propertyLoader == null) {
                throw new ConfigException("consul.fileExtension setting error, The loader was not found");
            }
            InputStream pull = client.pull(config);
            Optional.ofNullable(pull)
                    .map(e -> propertyLoader.load("remote.consul." + fileExtension, e))
                    .ifPresent(e -> context.get().getOriginal().load(() -> context.get().withSources(e), this::consulFinish));
            handler.finish(context, config);
            try {
                client.addListener(context, (c1, c2) -> this.passive(c1, null, c2), config);
            } catch (Exception e) {
                LOGGER.error("passive consul remote started error....");
            }
        } else {
            throw new ConfigException("consul config is null");
        }
    }

    private void consulFinish(final Supplier<Context> contextSupplier, final Config config) {
        LOGGER.info("consul loader config {}:{}", config != null ? config.prefix() : "", config);
    }

    @Override
    public void passive(final Supplier<ConfigLoader.Context> context, final ConfigLoader.PassiveHandler<Config> handler, final Config config) {
        if (config instanceof ConsulPassiveConfig) {
            ConsulPassiveConfig consulPassiveConfig = (ConsulPassiveConfig) config;
            String value = consulPassiveConfig.getValue();
            if (StringUtils.isBlank(value)) {
                return;
            }
            PropertyLoader propertyLoader = LOADERS.get(consulPassiveConfig.getFileExtension());
            if (propertyLoader == null) {
                throw new ConfigException("consul.fileExtension setting error, The loader was not found");
            }
            InputStream inputStream = new ByteArrayInputStream(value.getBytes());
            Optional.of(inputStream)
                    .map(e -> propertyLoader.load(consulPassiveConfig.fileName(), e))
                    .ifPresent(e -> e.forEach(x -> x.getKeys().forEach(t -> ConfigEnv.getInstance().stream()
                            .filter(c -> t.startsWith(c.prefix())).forEach(c -> {
                                Object o = c.getSource().get(t);
                                EventData data = null;
                                if (Objects.isNull(o)) {
                                    data = new AddData(t, x.getValue(t));
                                } else if (!Objects.equals(o, x.getValue(t))) {
                                    data = new ModifyData(t, x.getValue(t));
                                }
                                push(context, data);
                            }))));
        }
    }

    private void check(final ConsulConfig config) {
        if (StringUtils.isBlank(config.getHostAndPorts()) && StringUtils.isBlank(config.getHostAndPort())) {
            throw new ConfigException("consul.hostAndPorts is null");
        }
        if (StringUtils.isBlank(config.getFileExtension())) {
            throw new ConfigException("consul.fileExtension is null");
        }
        if (StringUtils.isBlank(config.getKey())) {
            throw new ConfigException("consul.key is null");
        }
    }

}
