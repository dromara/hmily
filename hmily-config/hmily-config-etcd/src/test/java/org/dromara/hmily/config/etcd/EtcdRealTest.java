package org.dromara.hmily.config.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.config.api.event.*;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

/**
 * @author lilang
 * @date 2020-09-23 23:39
 **/
public class EtcdRealTest {

    private EtcdConfig config;

    private Client client;

    private EtcdConfigLoader etcdConfigLoader;

    @Before
    public void setUp() throws Exception {
        config = buildConfig();
        client = Client.builder().endpoints(config.getServer()).build();
        etcdConfigLoader = new EtcdConfigLoader();
    }

    @Test
    public void testPull() throws IOException, ExecutionException, InterruptedException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/hmily-etcd.yml");
        int available = resourceAsStream.available();
        byte[] bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.getKVClient().put(ByteSequence.fromString(config.getKey()), ByteSequence.fromBytes(bytes)).get();
        EtcdClient etcdClient = EtcdClient.getInstance(buildConfig());
        InputStream is = etcdClient.pull(config);
        byte[] remoteConfig = IOUtils.readFully(is, available, false);
        Assert.assertArrayEquals(bytes, remoteConfig);
        client.getKVClient().delete(ByteSequence.fromString(config.getKey())).get();
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
                etcdConfigLoader.load(context, (context1, cfg) -> {
                    System.out.println("config: ==> "  + cfg);
                });
            }
        }));

        Thread.sleep(5000);


//        new Thread(() -> {
//            while(true) {
//                try {
//                    client.getWatchClient().watch(ByteSequence.fromString(config.getKey())).listen().getEvents().stream().forEach(watchEvent -> {
//                        WatchEvent.EventType eventType = watchEvent.getEventType();
//                        KeyValue keyValue = watchEvent.getKeyValue();
//                        KeyValue prevKV = watchEvent.getPrevKV();
//                        EventData eventData = null;
//                        switch (eventType) {
//                            case PUT:
//                                eventData = new AddData(keyValue.getKey().toStringUtf8(), keyValue.getValue().toStringUtf8());
//                                break;
//                            case DELETE:
//                                eventData = new RemoveData(prevKV.getKey().toStringUtf8(), null);
//                                break;
//                            default:
//                                break;
//                        }
//                        System.out.println(eventData);
//                    });
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }).start();

        changeRemoteData();
    }

    private void changeRemoteData() throws IOException, ExecutionException, InterruptedException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/hmily-etcd-update.yml");
        int available = resourceAsStream.available();
        byte[] bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.getKVClient().put(ByteSequence.fromString(config.getKey()), ByteSequence.fromBytes(bytes)).get();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());

        resourceAsStream = getClass().getResourceAsStream("/hmily-etcd.yml");
        available = resourceAsStream.available();
        bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.getKVClient().put(ByteSequence.fromString(config.getKey()), ByteSequence.fromBytes(bytes)).get();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());

        client.getKVClient().delete(ByteSequence.fromString(config.getKey())).get();

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
        resourceAsStream = getClass().getResourceAsStream("/hmily-etcd-update.yml");
        available = resourceAsStream.available();
        bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.getKVClient().put(ByteSequence.fromString(config.getKey()), ByteSequence.fromBytes(bytes)).get();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals("xiaoyu1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getAppName());
        Assert.assertEquals("kryo1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getSerializer());
        Assert.assertEquals("threadLocal1", ConfigEnv.getInstance().getConfig(HmilyConfig.class).getContextTransmittalMode());
    }


    private EtcdConfig buildConfig() {
        EtcdConfig etcdConfig = new EtcdConfig();
        etcdConfig.setServer("http://localhost:2379");
        etcdConfig.setKey("test");
        etcdConfig.setFileExtension("yml");
        return etcdConfig;
    }

}
