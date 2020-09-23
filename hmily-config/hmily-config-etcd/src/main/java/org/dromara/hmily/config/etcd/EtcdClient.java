package org.dromara.hmily.config.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.watch.WatchEvent;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.event.AddData;
import org.dromara.hmily.config.api.event.EventData;
import org.dromara.hmily.config.api.event.RemoveData;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
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
public class EtcdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdClient.class);

    private Client client;

    /**
     * Pull input stream.
     *
     * @param config the config
     * @return the input stream
     */
    public InputStream pull(final EtcdConfig config) {
        Client client = Client.builder().endpoints(config.getServer()).build();
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
        client.getWatchClient().watch(ByteSequence.fromString(config.getKey())).listen().getEvents().stream().forEach(watchEvent -> {
            WatchEvent.EventType eventType = watchEvent.getEventType();
            KeyValue keyValue = watchEvent.getKeyValue();
            KeyValue prevKV = watchEvent.getPrevKV();
            EventData eventData = null;
            switch (eventType) {
                case PUT:
                    eventData = new AddData(keyValue.getKey().toStringUtf8(), keyValue.getValue().toStringUtf8());
                    break;
                case DELETE:
                    eventData = new RemoveData(prevKV.getKey().toStringUtf8(), null);
                    break;
                default:
                    break;
            }
            Optional.of(eventData).ifPresent(e -> {
                EtcdPassiveConfig etcdPassiveConfig = new EtcdPassiveConfig();
                etcdPassiveConfig.setKey(config.getKey());
                etcdPassiveConfig.setFileExtension(config.getFileExtension());
                etcdPassiveConfig.setValue(e);
                passiveHandler.passive(context, etcdPassiveConfig);
            });
        });
        LOGGER.info("passive Etcd remote started....");
    }
}
