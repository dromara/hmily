package org.dromara.hmily.config.etcd;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.watch.WatchEvent;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.ConfigScan;
import org.dromara.hmily.config.api.event.*;
import org.dromara.hmily.config.loader.ConfigLoader;
import org.dromara.hmily.config.loader.ServerConfigLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
        EtcdClient etcdClient = new EtcdClient();
        InputStream is = etcdClient.pull(config);
        byte[] remoteConfig = IOUtils.readFully(is, available, false);
        Assert.assertArrayEquals(bytes, remoteConfig);
    }

    @Test
    public void testLoad() throws InterruptedException, IOException, ExecutionException {
        ConfigScan.scan();
        ConfigEnv.getInstance().addEvent(new EventConsumer<RemoveData>() {
            @Override
            public void accept(RemoveData data) {
                System.out.println(data);
            }

            @Override
            public String properties() {
                return "hmily.config.*";
            }
        });

        ConfigEnv.getInstance().addEvent(new EventConsumer<AddData>() {
            @Override
            public void accept(AddData data) {
                System.out.println(data);
            }

            @Override
            public String properties() {
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

        Thread.sleep(20000);
    }

    private void changeRemoteData() throws IOException, ExecutionException, InterruptedException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/hmily-etcd-update.yml");
        int available = resourceAsStream.available();
        byte[] bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.getKVClient().put(ByteSequence.fromString(config.getKey()), ByteSequence.fromBytes(bytes)).get();

        resourceAsStream = getClass().getResourceAsStream("/hmily-etcd.yml");
        available = resourceAsStream.available();
        bytes = IOUtils.readFully(resourceAsStream, available, false);
        client.getKVClient().put(ByteSequence.fromString(config.getKey()), ByteSequence.fromBytes(bytes)).get();

        client.getKVClient().delete(ByteSequence.fromString(config.getKey())).get();
    }


    private EtcdConfig buildConfig() {
        EtcdConfig etcdConfig = new EtcdConfig();
        etcdConfig.setServer("http://localhost:2379");
        etcdConfig.setKey("test");
        etcdConfig.setFileExtension("yml");
        return etcdConfig;
    }

}
