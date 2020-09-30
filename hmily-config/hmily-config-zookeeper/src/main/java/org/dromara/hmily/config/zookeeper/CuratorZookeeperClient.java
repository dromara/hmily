/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.config.zookeeper;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.OperationTimeoutException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.zookeeper.handler.CuratorZookeeperExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Curator zookeeper client.
 *
 * @author xiaoyu
 */
public final class CuratorZookeeperClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CuratorZookeeperClient.class);

    private static final Map<String, CuratorCache> CACHES = new HashMap<>();

    private static volatile CuratorZookeeperClient instance;

    private CuratorFramework client;

    private CuratorZookeeperClient() {
    }
    
    /**
     * Gets instance.
     *
     * @param zookeeperConfig the zookeeper config
     * @return the instance
     */
    public static CuratorZookeeperClient getInstance(final ZookeeperConfig zookeeperConfig) {
        if (instance == null) {
            synchronized (CuratorZookeeperClient.class) {
                if (instance == null) {
                    instance = new CuratorZookeeperClient();
                    instance.initCuratorClient(zookeeperConfig);
                }
            }
        }
        return instance;
    }

    private void initCuratorClient(final ZookeeperConfig zookeeperConfig) {
        int retryIntervalMilliseconds = zookeeperConfig.getRetryIntervalMilliseconds();
        int maxRetries = zookeeperConfig.getMaxRetries();
        int timeToLiveSeconds = zookeeperConfig.getTimeToLiveSeconds();
        int operationTimeoutMilliseconds = zookeeperConfig.getOperationTimeoutMilliseconds();
        String digest = zookeeperConfig.getDigest();
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zookeeperConfig.getServerList())
                .retryPolicy(new ExponentialBackoffRetry(retryIntervalMilliseconds, maxRetries, retryIntervalMilliseconds * maxRetries));
        if (0 != timeToLiveSeconds) {
            builder.sessionTimeoutMs(timeToLiveSeconds * 1000);
        }
        if (0 != operationTimeoutMilliseconds) {
            builder.connectionTimeoutMs(operationTimeoutMilliseconds);
        }
        if (!Strings.isNullOrEmpty(digest)) {
            builder.authorization("digest", digest.getBytes(Charsets.UTF_8))
                    .aclProvider(new ACLProvider() {

                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(final String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        client = builder.build();
        client.start();
        try {
            if (!client.blockUntilConnected(retryIntervalMilliseconds * maxRetries, TimeUnit.MILLISECONDS)) {
                client.close();
                throw new OperationTimeoutException();
            }
        } catch (final InterruptedException | OperationTimeoutException ex) {
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    /**
     * Pull input stream.
     *
     * @param path the path
     * @return the input stream
     */
    public InputStream pull(final String path) {
        String content = get(path);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("zookeeper content {}", content);
        }
        if (StringUtils.isBlank(content)) {
            return null;
        }
        return new ByteArrayInputStream(content.getBytes());
    }
    
    /**
     * Add listener.
     *
     * @param context        the context
     * @param passiveHandler the passive handler
     * @param config         the config
     * @throws Exception the exception
     */
    public void addListener(final Supplier<ConfigLoader.Context> context, final ConfigLoader.PassiveHandler<ZkPassiveConfig> passiveHandler, final ZookeeperConfig config) throws Exception {
        if (!config.isPassive()) {
            return;
        }
        if (client == null) {
            LOGGER.warn("zookeeper client is null...");
        }
        // Use CuratorCache to monitor and find that the lower version of zk cannot monitor the message.
        // But using this high version marked as @Deprecated can receive messages normally.。
        //@see CuratorCache
        NodeCache cache = new NodeCache(client, config.getPath());
        cache.getListenable().addListener(() -> {
            byte[] data = cache.getCurrentData().getData();
            String string = new String(data, StandardCharsets.UTF_8);
            ZkPassiveConfig zkPassiveConfig = new ZkPassiveConfig();
            zkPassiveConfig.setPath(config.getPath());
            zkPassiveConfig.setFileExtension(config.getFileExtension());
            zkPassiveConfig.setValue(string);
            passiveHandler.passive(context, zkPassiveConfig);
        });
        cache.start();
        LOGGER.info("passive zookeeper remote started....");
    }
    
    /**
     * Get string.
     *
     * @param path the path
     * @return the string
     */
    public String get(final String path) {
        CuratorCache cache = findTreeCache(path);
        if (null == cache) {
            return getDirectly(path);
        }
        Optional<ChildData> resultInCache = cache.get(path);
        if (resultInCache.isPresent()) {
            return null == resultInCache.get().getData() ? null : new String(resultInCache.get().getData(), Charsets.UTF_8);
        }
        return getDirectly(path);
    }
    
    /**
     * Persist.
     *
     * @param key   the key
     * @param value the value
     */
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(Charsets.UTF_8));
            } else {
                update(key, value);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }

    private void update(final String key, final String value) {
        try {
            TransactionOp transactionOp = client.transactionOp();
            client.transaction().forOperations(transactionOp.check().forPath(key), transactionOp.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8)));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }

    @Override
    public void close() {
        CACHES.values().forEach(CuratorCache::close);
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }

    private CuratorCache findTreeCache(final String key) {
        return CACHES.entrySet().stream().filter(entry -> key.startsWith(entry.getKey())).findFirst().map(Map.Entry::getValue).orElse(null);
    }

    private boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (final Exception ex) {
            CuratorZookeeperExceptionHandler.handleException(ex);
            return false;
        }
    }

    private String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), Charsets.UTF_8);
        } catch (final Exception ex) {
            CuratorZookeeperExceptionHandler.handleException(ex);
            return null;
        }
    }

    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
