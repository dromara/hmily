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

package com.hmily.tcc.admin.spi;

import com.google.common.base.Splitter;
import com.hmily.tcc.admin.service.CompensationService;
import com.hmily.tcc.admin.service.compensate.*;
import com.hmily.tcc.common.jedis.JedisClient;
import com.hmily.tcc.common.jedis.JedisClientCluster;
import com.hmily.tcc.common.jedis.JedisClientSingle;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * CompensationConfiguration.
 * @author xiaoyu
 */
@Configuration
public class CompensationConfiguration {

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

        @Autowired
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
            final Boolean cluster = env.getProperty("compensation.redis.cluster", Boolean.class);
            if (cluster) {
                final String clusterUrl = env.getProperty("compensate.redis.clusterUrl");
                final Set<HostAndPort> hostAndPorts = Splitter.on(clusterUrl)
                        .splitToList(";").stream()
                        .map(HostAndPort::parseString).collect(Collectors.toSet());
                JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
                jedisClient = new JedisClientCluster(jedisCluster);
            } else {
                final String password = env.getProperty("compensation.redis.password");
                final String port = env.getProperty("compensation.redis.port");
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

        @Autowired
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

        @Autowired
        ZookeeperRecoverConfiguration(final Environment env, final ObjectSerializer objectSerializer) {
            this.env = env;
            this.objectSerializer = objectSerializer;
        }

        @Bean
        @Qualifier("zookeeperTransactionRecoverService")
        public CompensationService zookeeperTransactionRecoverService() {
            ZooKeeper zooKeeper = null;
            try {
                final String host = env.getProperty("compensation.zookeeper.host");
                final String sessionTimeOut = env.getProperty("compensation.zookeeper.sessionTimeOut");
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
            MongoCredential credential = MongoCredential.createScramSha1Credential(
                    env.getProperty("compensation.mongo.userName"),
                    env.getProperty("compensation.mongo.dbName"),
                    env.getProperty("compensation.mongo.password").toCharArray());
            clientFactoryBean.setCredentials(new MongoCredential[]{credential});
            List<String> urls = Splitter.on(",").trimResults().splitToList(env.getProperty("compensation.mongo.url"));
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
                mongoTemplate = new MongoTemplate(clientFactoryBean.getObject(), env.getProperty("compensation.mongo.dbName"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new MongoCompensationServiceImpl(mongoTemplate);
        }

    }

}
