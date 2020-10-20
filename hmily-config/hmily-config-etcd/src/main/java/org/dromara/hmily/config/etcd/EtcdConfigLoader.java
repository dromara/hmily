package org.dromara.hmily.config.etcd;

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
 * etcd config loader.
 *
 * @author lilang
 **/
@HmilySPI("etcd")
public class EtcdConfigLoader implements ConfigLoader<EtcdConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdConfigLoader.class);

    private static final Map<String, PropertyLoader> LOADERS = new HashMap<>();

    private EtcdClient client;

    static {
        LOADERS.put("yml", new YamlPropertyLoader());
        LOADERS.put("properties", new PropertiesLoader());
    }

    /**
     * Instantiates a new Etcd config loader.
     */
    public EtcdConfigLoader() {
    }

    /**
     * Instantiates a new Etcd config loader.
     *
     * @param client the client
     */
    public EtcdConfigLoader(final EtcdClient client) {
        this();
        this.client = client;
    }

    @Override
    public void load(final Supplier<Context> context, final LoaderHandler<EtcdConfig> handler) {
        LoaderHandler<EtcdConfig> etcdHandler = (c, config) -> etcdLoad(c, handler, config);
        againLoad(context, etcdHandler, EtcdConfig.class);
    }

    @Override
    public void passive(final Supplier<Context> context, final PassiveHandler<Config> handler, final Config config) {
        if (config instanceof EtcdPassiveConfig) {
            EtcdPassiveConfig etcdPassiveConfig = (EtcdPassiveConfig) config;
            String value = etcdPassiveConfig.getValue();
            if (StringUtils.isBlank(value)) {
                return;
            }
            PropertyLoader propertyLoader = LOADERS.get(etcdPassiveConfig.getFileExtension());
            if (propertyLoader == null) {
                throw new ConfigException("etcd.fileExtension setting error, The loader was not found");
            }
            InputStream inputStream = new ByteArrayInputStream(value.getBytes());
            Optional.of(inputStream)
                    .map(e -> propertyLoader.load(etcdPassiveConfig.fileName(), e))
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

    private void etcdLoad(final Supplier<Context> context, final LoaderHandler<EtcdConfig> handler, final EtcdConfig config) {
        if (config != null) {
            check(config);
            if (Objects.isNull(client)) {
                client = EtcdClient.getInstance(config);
            }
            if (config.isUpdate()) {
                client.put(config.getKey(), FileUtils.readYAML(config.getUpdateFileName()));
            }
            LOGGER.info("loader etcd config: {}", config);
            String fileExtension = config.getFileExtension();
            PropertyLoader propertyLoader = LOADERS.get(fileExtension);
            if (propertyLoader == null) {
                throw new ConfigException("etcd.fileExtension setting error, The loader was not found");
            }
            InputStream pull = client.pull(config);
            Optional.ofNullable(pull)
                    .map(e -> propertyLoader.load("remote.etcd." + fileExtension, e))
                    .ifPresent(e -> context.get().getOriginal().load(() -> context.get().withSources(e), this::etcdFinish));
            handler.finish(context, config);
            try {
                client.addListener(context, (c1, c2) -> this.passive(c1, null, c2), config);
            } catch (Exception e) {
                LOGGER.error("passive etcd remote started error....");
            }
        } else {
            throw new ConfigException("etcd config is null");
        }
    }

    private void etcdFinish(final Supplier<Context> context, final Config config) {
        LOGGER.info("etcd loader config {}:{}", config != null ? config.prefix() : "", config);
    }

    private void check(final EtcdConfig config) {
        if (StringUtils.isBlank(config.getServer())) {
            throw new ConfigException("etcd.server is null");
        }
        if (StringUtils.isBlank(config.getFileExtension())) {
            throw new ConfigException("etcd.fileExtension is null");
        }
        if (StringUtils.isBlank(config.getKey())) {
            throw new ConfigException("etcd.key is null");
        }
    }
}
