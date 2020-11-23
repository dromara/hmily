package org.dromara.hmily.config.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * etcd client.
 *
 * @author lilang
 **/
public final class EtcdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdClient.class);

    private Client client;

    private EtcdClient() {
    }

    /**
     * set client.
     *
     * @param client client
     */
    public void setClient(final Client client) {
        this.client = client;
    }

    /**
     * get instance of EtcdClient.
     *
     * @param config etcdConfig
     * @return etcd Client
     */
    public static EtcdClient getInstance(final EtcdConfig config) {
        Client client = Client.builder().endpoints(config.getServer()).build();
        EtcdClient etcdClient = new EtcdClient();
        etcdClient.setClient(client);
        return etcdClient;
    }

    /**
     * Pull input stream.
     *
     * @param config the config
     * @return the input stream
     */
    public InputStream pull(final EtcdConfig config) {
        if (client == null) {
            client = Client.builder().endpoints(config.getServer()).build();
        }
        try {
            CompletableFuture<GetResponse> future = client.getKVClient().get(ByteSequence.fromString(config.getKey()));
            List<KeyValue> kvs;
            if (config.getTimeoutMs() > 0L) {
                kvs = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS).getKvs();
            } else {
                kvs = future.get().getKvs();
            }
            if (CollectionUtils.isNotEmpty(kvs)) {
                String content = kvs.get(0).getValue().toStringUtf8();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("etcd content {}", content);
                }
                if (StringUtils.isBlank(content)) {
                    return null;
                }
                return new ByteArrayInputStream(content.getBytes());
            }
            return null;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ConfigException(e);
        }
    }

    /**
     * put config content.
     *
     * @param key config key
     * @param content config content
     */
    public void put(final String key, final String content) {
        try {
            client.getKVClient().put(ByteSequence.fromString(key), ByteSequence.fromString(content)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ConfigException(e);
        }
    }

    /**
     * Add listener.
     *
     * @param context        the context
     * @param passiveHandler the passive handler
     * @param config         the config
     * @throws InterruptedException exception
     */
    void addListener(final Supplier<ConfigLoader.Context> context, final ConfigLoader.PassiveHandler<EtcdPassiveConfig> passiveHandler, final EtcdConfig config) throws InterruptedException {
        if (!config.isPassive()) {
            return;
        }
        if (client == null) {
            LOGGER.warn("Etcd client is null...");
        }
        new Thread(() -> {
            while (true) {
                try {
                    client.getWatchClient().watch(ByteSequence.fromString(config.getKey())).listen().getEvents().stream().forEach(watchEvent -> {
                        KeyValue keyValue = watchEvent.getKeyValue();
                        EtcdPassiveConfig etcdPassiveConfig = new EtcdPassiveConfig();
                        etcdPassiveConfig.setKey(config.getKey());
                        etcdPassiveConfig.setFileExtension(config.getFileExtension());
                        etcdPassiveConfig.setValue(keyValue.getValue() != null ? keyValue.getValue().toStringUtf8() : null);
                        passiveHandler.passive(context, etcdPassiveConfig);
                    });
                } catch (InterruptedException e) {
                    LOGGER.error("", e);
                }
            }
        }).start();

        LOGGER.info("passive Etcd remote started....");
    }
}
