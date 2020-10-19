package org.dromara.hmily.config.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.config.api.event.EventConsumer;
import org.dromara.hmily.config.api.event.ModifyData;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author lilang
 * @date 2020-09-23 23:39
 **/
public class ConsulRealTest {

    private ConsulConfig config;

    private Consul client;

    private ConsulConfigLoader consulConfigLoader;

    @Before
    public void setUp() throws Exception {
        config = buildConfig();
        client = Consul.builder().withHostAndPort(HostAndPort.fromString(config.getHostAndPort())).build().newClient();
        consulConfigLoader = new ConsulConfigLoader();
    }

    private Collection<HostAndPort> buildHostAndPortList(String hostAndPorts) {
        if (StringUtils.isNoneBlank(hostAndPorts)) {
            String[] hostAndPortArray = hostAndPorts.split(",");
            List<HostAndPort> hostAndPortList = new ArrayList<>();
            for (String hostAndPort : hostAndPortArray) {
                hostAndPortList.add(HostAndPort.fromString(hostAndPort));
            }
            return hostAndPortList;
        } else {
            return Collections.emptyList();
        }
    }

    @Test
    public void testPull() throws IOException, ExecutionException, InterruptedException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/hmily-consul.yml");
        int available = resourceAsStream.available();
        byte[] bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.keyValueClient().putValue(config.getKey(), new String(bytes));
        ConsulClient consulClient = ConsulClient.getInstance(buildConfig());
        InputStream is = consulClient.pull(config);
        byte[] remoteConfig = IOUtils.readFully(is, available, false);
        Assert.assertArrayEquals(bytes, remoteConfig);
        client.keyValueClient().deleteKey(config.getKey());
    }

    @Test
    public void testLoad() throws InterruptedException, IOException, ExecutionException {
        ConfigScan.scan();
        ConfigEnv.getInstance().addEvent(new EventConsumer<ModifyData>() {
            @Override
            public void accept(ModifyData data) {
                System.out.println(data);
            }

            @Override
            public String regex() {
                return "hmily.config.*";
            }
        });
        ServerConfigLoader loader = new ServerConfigLoader();
        loader.load(ConfigLoader.Context::new, ((context, config1) -> {
            System.out.println("config:---->" + config1);
            if (config1 != null) {
                consulConfigLoader.load(context, (context1, cfg) -> {
                    System.out.println("config: ==> " + cfg);
                });
            }
        }));

        Thread.sleep(5000);

        changeRemoteData();
    }

    private void changeRemoteData() throws IOException, ExecutionException, InterruptedException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/hmily-consul-update.yml");
        int available = resourceAsStream.available();
        byte[] bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.keyValueClient().putValue(config.getKey(), new String(bytes));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());

        resourceAsStream = getClass().getResourceAsStream("/hmily-consul.yml");
        available = resourceAsStream.available();
        bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.keyValueClient().putValue(config.getKey(), new String(bytes));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());

        client.keyValueClient().deleteKey(config.getKey());

        // 删除 目前相当于无操作，当前生效的还是删除前的配置
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());

        // test add after delete
        resourceAsStream = getClass().getResourceAsStream("/hmily-consul-update.yml");
        available = resourceAsStream.available();
        bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.keyValueClient().putValue(config.getKey(), new String(bytes));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());
    }


    private ConsulConfig buildConfig() {
        ConsulConfig consulConfig = new ConsulConfig();
        consulConfig.setHostAndPort("localhost:8500");
        consulConfig.setKey("test");
        consulConfig.setFileExtension("yml");
        return consulConfig;
    }

}
