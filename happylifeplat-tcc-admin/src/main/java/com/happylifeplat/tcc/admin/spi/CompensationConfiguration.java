/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.happylifeplat.tcc.admin.spi;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Splitter;
import com.happylifeplat.tcc.admin.service.CompensationService;
import com.happylifeplat.tcc.admin.service.compensate.FileCompensationServiceImpl;
import com.happylifeplat.tcc.admin.service.compensate.JdbcCompensationServiceImpl;
import com.happylifeplat.tcc.admin.service.compensate.MongoCompensationServiceImpl;
import com.happylifeplat.tcc.admin.service.compensate.RedisCompensationServiceImpl;
import com.happylifeplat.tcc.admin.service.compensate.ZookeeperCompensationServiceImpl;
import com.happylifeplat.tcc.common.jedis.JedisClient;
import com.happylifeplat.tcc.common.jedis.JedisClientCluster;
import com.happylifeplat.tcc.common.jedis.JedisClientSingle;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
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
 * @author xiaoyu
 */
@Configuration
public class CompensationConfiguration {

    /**
     * spring.profiles.active = {}
     */
    @Configuration
    @Profile("db")
    static class JdbcRecoverConfiguration {

        private final Environment env;

        @Autowired
        public JdbcRecoverConfiguration(Environment env) {
            this.env = env;
        }

        @Bean
        public DataSource dataSource() {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName(env.getProperty("compensation.db.driver"));
            dataSource.setUrl(env.getProperty("compensation.db.url"));
            //用户名
            dataSource.setUsername(env.getProperty("compensation.db.username"));
            //密码
            dataSource.setPassword(env.getProperty("compensation.db.password"));
            dataSource.setInitialSize(2);
            dataSource.setMaxActive(20);
            dataSource.setMinIdle(0);
            dataSource.setMaxWait(60000);
            dataSource.setValidationQuery("SELECT 1");
            dataSource.setTestOnBorrow(false);
            dataSource.setTestWhileIdle(true);
            dataSource.setPoolPreparedStatements(false);
            return dataSource;
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

        @Autowired
        public RedisRecoverConfiguration(Environment env) {
            this.env = env;
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

            return new RedisCompensationServiceImpl(jedisClient);
        }


    }

    @Configuration
    @Profile("file")
    static class FileRecoverConfiguration {

        @Bean
        @Qualifier("fileTransactionRecoverService")
        public CompensationService fileTransactionRecoverService() {
            return new FileCompensationServiceImpl();
        }

    }

    @Configuration
    @Profile("zookeeper")
    static class ZookeeperRecoverConfiguration {

        private final Environment env;

        @Autowired
        public ZookeeperRecoverConfiguration(Environment env) {
            this.env = env;
        }

        private static final Lock LOCK = new ReentrantLock();


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

            return new ZookeeperCompensationServiceImpl(zooKeeper);
        }

    }

    @Configuration
    @Profile("mongo")
    static class MongoRecoverConfiguration {

        private final Environment env;

        @Autowired
        public MongoRecoverConfiguration(Environment env) {
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
            clientFactoryBean.setCredentials(new MongoCredential[]{
                    credential
            });
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
