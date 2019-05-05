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

package org.dromara.hmily.admin.spi;

import com.google.common.base.Splitter;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.dromara.hmily.admin.interceptor.AuthInterceptor;
import org.dromara.hmily.admin.service.CompensationService;
import org.dromara.hmily.admin.service.compensate.FileCompensationServiceImpl;
import org.dromara.hmily.admin.service.compensate.JdbcCompensationServiceImpl;
import org.dromara.hmily.admin.service.compensate.MongoCompensationServiceImpl;
import org.dromara.hmily.admin.service.compensate.RedisCompensationServiceImpl;
import org.dromara.hmily.admin.service.compensate.ZookeeperCompensationServiceImpl;
import org.dromara.hmily.common.jedis.JedisClient;
import org.dromara.hmily.common.jedis.JedisClientCluster;
import org.dromara.hmily.common.jedis.JedisClientSentinel;
import org.dromara.hmily.common.jedis.JedisClientSingle;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.common.utils.extension.ExtensionLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * CompensationConfiguration.
 *
 * @author xiaoyu
 */
@Configuration
public class HmilyAdminConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
            }
        };
    }

    @Configuration
    static class SerializerConfiguration {

        private final Environment env;

        @Autowired
        SerializerConfiguration(final Environment env) {
            this.env = env;
        }

        @Bean
        public ObjectSerializer objectSerializer() {
            return ExtensionLoader.getExtensionLoader(ObjectSerializer.class)
                    .getActivateExtension(env.getProperty("compensation.serializer.support"));
        }
    }

    /**
     * spring.profiles.active = {}.
     */
    @Configuration
    @Profile("db")
    static class JdbcRecoverConfiguration {

        private final Environment env;

        @Autowired
        JdbcRecoverConfiguration(final Environment env) {
            this.env = env;
        }

        @Bean
        public DataSource dataSource() {
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setDriverClassName(env.getProperty("compensation.db.driver"));
            hikariDataSource.setJdbcUrl(env.getProperty("compensation.db.url"));
            //用户名
            hikariDataSource.setUsername(env.getProperty("compensation.db.username"));
            //密码
            hikariDataSource.setPassword(env.getProperty("compensation.db.password"));
            hikariDataSource.setMinimumIdle(5);
            hikariDataSource.setMaximumPoolSize(10);
            return hikariDataSource;
        }

        @Bean
        @Qualifier("jdbcTransactionRecoverService")
        public CompensationService jdbcTransactionRecoverService() {
            JdbcCompensationServiceImpl jdbcTransactionRecoverService = new JdbcCompensationServiceImpl();
            jdbcTransactionRecoverService.setDbType(env.getProperty("compensation.db.driver"));
            return jdbcTransactionRecoverService;
        }
    }

    @Configuration
    @Profile("redis")
    static class RedisRecoverConfiguration {

        private final Environment env;

        private final ObjectSerializer objectSerializer;

        @Autowired(required = false)
        RedisRecoverConfiguration(final Environment env, final ObjectSerializer objectSerializer) {
            this.env = env;
            this.objectSerializer = objectSerializer;
        }

        @Bean
        @Qualifier("redisTransactionRecoverService")
        public CompensationService redisTransactionRecoverService() {

            JedisPool jedisPool;
            JedisPoolConfig config = new JedisPoolConfig();
            JedisClient jedisClient;
            final Boolean cluster = env.getProperty("compensation.redis.cluster", Boolean.class, Boolean.FALSE);
            final Boolean sentinel = env.getProperty("compensation.redis.sentinel", Boolean.class, Boolean.FALSE);
            final String password = env.getProperty("compensation.redis.password");
            if (cluster) {
                final String clusterUrl = env.getProperty("compensation.redis.clusterUrl");
                assert clusterUrl != null;
                final Set<HostAndPort> hostAndPorts = Splitter.on(";")
                        .splitToList(clusterUrl).stream()
                        .map(HostAndPort::parseString).collect(Collectors.toSet());
                JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
                jedisClient = new JedisClientCluster(jedisCluster);
            } else if (sentinel) {
                final String sentinelUrl = env.getProperty("compensation.redis.sentinelUrl");
                assert sentinelUrl != null;
                final Set<String> hostAndPorts =
                        new HashSet<>(Splitter.on(";")
                                .splitToList(sentinelUrl));
                final String master = env.getProperty("compensation.redis.master");
                JedisSentinelPool pool =
                        new JedisSentinelPool(master, hostAndPorts,
                                config, password);
                jedisClient = new JedisClientSentinel(pool);
            } else {
                final String port = env.getProperty("compensation.redis.port", "6379");
                final String hostName = env.getProperty("compensation.redis.hostName");
                if (StringUtils.isNoneBlank(password)) {
                    jedisPool = new JedisPool(config, hostName,
                            Integer.parseInt(port), 30, password);
                } else {
                    jedisPool = new JedisPool(config, hostName,
                            Integer.parseInt(port), 30);
                }
                jedisClient = new JedisClientSingle(jedisPool);
            }
            return new RedisCompensationServiceImpl(jedisClient, objectSerializer);
        }
    }

    @Configuration
    @Profile("file")
    static class FileRecoverConfiguration {

        private final ObjectSerializer objectSerializer;

        @Autowired(required = false)
        FileRecoverConfiguration(final ObjectSerializer objectSerializer) {
            this.objectSerializer = objectSerializer;
        }

        @Bean
        @Qualifier("fileTransactionRecoverService")
        public CompensationService fileTransactionRecoverService() {
            return new FileCompensationServiceImpl(objectSerializer);
        }

    }

    @Configuration
    @Profile("zookeeper")
    static class ZookeeperRecoverConfiguration {

        private static final Lock LOCK = new ReentrantLock();

        private final Environment env;

        private final ObjectSerializer objectSerializer;

        @Autowired(required = false)
        ZookeeperRecoverConfiguration(final Environment env, final ObjectSerializer objectSerializer) {
            this.env = env;
            this.objectSerializer = objectSerializer;
        }

        @Bean
        @Qualifier("zookeeperTransactionRecoverService")
        public CompensationService zookeeperTransactionRecoverService() {
            ZooKeeper zooKeeper = null;
            try {
                final String host = env.getProperty("compensation.zookeeper.host", "2181");
                final String sessionTimeOut = env.getProperty("compensation.zookeeper.sessionTimeOut", "3000");
                zooKeeper = new ZooKeeper(host, Integer.parseInt(sessionTimeOut), watchedEvent -> {
                    if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        // 放开闸门, wait在connect方法上的线程将被唤醒
                        LOCK.unlock();
                    }
                });
                LOCK.lock();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ZookeeperCompensationServiceImpl(zooKeeper, objectSerializer);
        }
    }

    @Configuration
    @Profile("mongo")
    static class MongoRecoverConfiguration {

        private final Environment env;

        @Autowired
        MongoRecoverConfiguration(final Environment env) {
            this.env = env;
        }

        @Bean
        @Qualifier("mongoTransactionRecoverService")
        public CompensationService mongoTransactionRecoverService() {
            MongoClientFactoryBean clientFactoryBean = new MongoClientFactoryBean();
            final String userName = env.getProperty("compensation.mongo.userName", "xiaoyu");
            final String dbName = env.getProperty("compensation.mongo.dbName", "col");
            final String password = env.getProperty("compensation.mongo.password", "123456");
            final String url = env.getProperty("compensation.mongo.url", "127.0.0.1");
            MongoCredential credential = MongoCredential.createScramSha1Credential(
                    userName,
                    dbName,
                    password.toCharArray());
            clientFactoryBean.setCredentials(new MongoCredential[]{credential});
            List<String> urls = Splitter.on(",").trimResults().splitToList(url);
            ServerAddress[] sds = new ServerAddress[urls.size()];
            for (int i = 0; i < sds.length; i++) {
                List<String> adds = Splitter.on(":").trimResults().splitToList(urls.get(i));
                InetSocketAddress address = new InetSocketAddress(adds.get(0), Integer.parseInt(adds.get(1)));
                sds[i] = new ServerAddress(address);
            }
            clientFactoryBean.setReplicaSetSeeds(sds);
            MongoTemplate mongoTemplate = null;
            try {
                clientFactoryBean.afterPropertiesSet();
                mongoTemplate = new MongoTemplate(Objects.requireNonNull(clientFactoryBean.getObject()),
                        dbName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new MongoCompensationServiceImpl(mongoTemplate);
        }

    }

}
