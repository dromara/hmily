package org.dromara.hmily.config.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.exception.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * etcd client.
 *
 * @author lilang
 **/
public class EtcdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdClient.class);

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
}
