package org.dromara.hmily.config.etcd;

import lombok.Data;
import org.dromara.hmily.config.api.AbstractConfig;
import org.dromara.hmily.config.api.event.EventData;

/**
 * etcd passive config.
 * @author lilang
 **/
@Data
public class EtcdPassiveConfig extends AbstractConfig {

    private EventData value;

    private String key;

    private String fileExtension;

    @Override
    public String prefix() {
        return "";
    }
}
