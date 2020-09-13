package org.dromara.hmily.config.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.Config;
import org.dromara.hmily.config.api.event.AddData;
import org.dromara.hmily.config.api.event.EventData;
import org.dromara.hmily.config.api.event.ModifyData;
import org.dromara.hmily.config.api.event.RemoveData;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.PropertyLoader;
import org.dromara.hmily.config.loader.properties.PropertiesLoader;
import org.dromara.hmily.config.loader.yaml.YamlPropertyLoader;
import org.dromara.hmily.spi.HmilySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The type apollo config loader.
 *
 * @author lilang
 */
@HmilySPI("apollo")
public class ApolloConfigLoader implements ConfigLoader<ApolloConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloConfigLoader.class);

    private static final Map<String, PropertyLoader> LOADERS = new HashMap<>();

    private ApolloClient client = new ApolloClient();

    static {
        LOADERS.put("yml", new YamlPropertyLoader());
        LOADERS.put("properties", new PropertiesLoader());
    }

    /**
     * Instantiates a new Apollo config loader.
     */
    public ApolloConfigLoader() {

    }

    /**
     * Instantiates a new Apollo config loader.
     *
     * @param apolloClient the apollo client
     */
    public ApolloConfigLoader(final ApolloClient apolloClient) {
        this.client = apolloClient;
    }

    @Override
    public void load(final Supplier<Context> context, final LoaderHandler<ApolloConfig> handler) {
        LoaderHandler<ApolloConfig> apolloHandler = (c, config) -> apolloLoad(c, handler, config);
        againLoad(context, apolloHandler, ApolloConfig.class);
    }

    @Override
    public void passive(final Supplier<Context> context, final PassiveHandler<Config> handler, final Config config) {
        if (config.isPassive()) {
            LOGGER.info("passive apollo remote started....");
            ApolloConfig apolloConfig = (ApolloConfig) config;
            com.ctrip.framework.apollo.Config appConfig = ConfigService.getConfig(apolloConfig.getNamespace() + "." + apolloConfig.getFileExtension());
            appConfig.addChangeListener(changeEvent -> {
                for (String key : changeEvent.changedKeys()) {
                    ConfigChange change = changeEvent.getChange(key);
                    EventData data = null;
                    switch (change.getChangeType()) {
                        case ADDED:
                            data = new AddData(change.getPropertyName(), change.getNewValue());
                            break;
                        case DELETED:
                            data = new RemoveData(change.getPropertyName(), change.getNewValue());
                            break;
                        case MODIFIED:
                            data = new ModifyData(change.getPropertyName(), change.getNewValue());
                            break;
                        default:
                            break;
                    }
                    push(context, data);
                }
            });
        }
    }

    private void apolloLoad(final Supplier<Context> context, final LoaderHandler<ApolloConfig> handler, final ApolloConfig config) {
        if (config != null) {
            check(config);
            LOGGER.info("loader apollo config: {}", config);
            String fileExtension = config.getFileExtension();
            PropertyLoader propertyLoader = LOADERS.get(fileExtension);
            if (propertyLoader == null) {
                throw new ConfigException("apollo.fileExtension setting error, The loader was not found");
            }
            InputStream pull = client.pull(config);
            Optional.ofNullable(pull)
                    .map(e -> propertyLoader.load("remote.apollo." + fileExtension, e))
                    .ifPresent(e -> context.get().getOriginal().load(() -> context.get().withSources(e), this::apolloFinish));
            handler.finish(context, config);
            passive(context, null, config);
        } else {
            throw new ConfigException("apollo config is null");
        }
    }

    private void check(final ApolloConfig config) {
        if (StringUtils.isBlank(config.getAppId())) {
            throw new ConfigException("apollo.appId is null");
        }
        if (StringUtils.isBlank(config.getConfigService())) {
            throw new ConfigException("apollo.configService is null");
        }
        if (!config.getConfigService().startsWith("http")) {
            throw new ConfigException("apollo.configService is http protocol address");
        }
        if (StringUtils.isBlank(config.getFileExtension())) {
            throw new ConfigException("apollo.fileExtension is null");
        }
        if (StringUtils.isBlank(config.getNamespace())) {
            throw new ConfigException("apollo.namespace is null");
        }
    }

    private void apolloFinish(final Supplier<Context> contextSupplier, final Config config) {
        LOGGER.info("apollo loader config {}:{}", config != null ? config.prefix() : "", config);
    }
}
