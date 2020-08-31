package org.dromara.hmily.config.apollo;

import org.dromara.hmily.common.utils.FileUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.*;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;

/**
 * Author:   lilang
 * Date:     2020-08-23 18:55
 * Description:
 **/
@RunWith(PowerMockRunner.class)
@PrepareForTest(ApolloClient.class)
public class ApolloConfigLoaderTest {

    private ApolloClient client = PowerMockito.mock(ApolloClient.class);

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        String str = FileUtils.readYAML("hmily-apollo.yml");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
        try {
            PowerMockito.when(client.pull(any())).thenReturn(byteArrayInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * Test apollo load.
     */
    @Test
    public void testApolloLoad() {
        ConfigScan.scan();
        ServerConfigLoader loader = new ServerConfigLoader();
        ApolloConfigLoader apolloConfigLoader = new ApolloConfigLoader(client);
        loader.load(ConfigLoader.Context::new, (context, config) -> {
            if (config != null) {
                if (StringUtils.isNoneBlank(config.getConfigMode())) {
                    String configMode = config.getConfigMode();
                    if (configMode.equals("apollo")) {
                        apolloConfigLoader.load(context, this::assertTest);
                    }
                }
            }
        });
    }

    private void assertTest(final Supplier<ConfigLoader.Context> supplier, final ApolloConfig apolloConfig) {
        Assert.assertNotNull(apolloConfig);
        Assert.assertEquals(apolloConfig.prefix(), "remote.apollo");
        Assert.assertEquals(apolloConfig.getAppId(), "10001234");
        HmilyServer server = ConfigEnv.getInstance().getConfig(HmilyServer.class);
        Assert.assertNotNull(server);
        Assert.assertEquals(server.getConfigMode(), "apollo");
        HmilyConfig config = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getAppName(), "xiaoyu");
        HmilyDatabaseConfig databaseConfig = ConfigEnv.getInstance().getConfig(HmilyDatabaseConfig.class);
        Assert.assertNotNull(databaseConfig);
        HmilyFileConfig fileConfig = ConfigEnv.getInstance().getConfig(HmilyFileConfig.class);
        Assert.assertNotNull(fileConfig);
        HmilyMetricsConfig metricsConfig = ConfigEnv.getInstance().getConfig(HmilyMetricsConfig.class);
        Assert.assertNotNull(metricsConfig);
        HmilyMongoConfig mongoConfig = ConfigEnv.getInstance().getConfig(HmilyMongoConfig.class);
        Assert.assertNotNull(mongoConfig);
        HmilyRedisConfig redisConfig = ConfigEnv.getInstance().getConfig(HmilyRedisConfig.class);
        Assert.assertNotNull(redisConfig);
        HmilyZookeeperConfig hmilyZookeeperConfig = ConfigEnv.getInstance().getConfig(HmilyZookeeperConfig.class);
        Assert.assertNotNull(hmilyZookeeperConfig);
    }
}
