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

//    private ApolloClient client = PowerMockito.mock(ApolloClient.class);

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
//        String str = FileUtils.readYAML("hmily-apollo.yml");
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
        try {
//            PowerMockito.when(client.pull(any())).thenReturn(byteArrayInputStream);
        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail();
        }
    }

    /**
     * Test apollo load.
     */
    @Test
    public void testApolloLoad() throws InterruptedException {
        ConfigScan.scan();
        ServerConfigLoader loader = new ServerConfigLoader();
        ApolloConfigLoader apolloConfigLoader = new ApolloConfigLoader();
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
        Thread.sleep(Integer.MAX_VALUE);
    }

    private void assertTest(final Supplier<ConfigLoader.Context> supplier, final ApolloConfig apolloConfig) {

    }
}
