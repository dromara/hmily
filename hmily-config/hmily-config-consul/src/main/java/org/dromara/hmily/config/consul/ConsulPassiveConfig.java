package org.dromara.hmily.config.consul;

import lombok.Data;
import org.dromara.hmily.config.api.AbstractConfig;

/**
 * consul passive config.
 * @author lilang
 **/
@Data
public class ConsulPassiveConfig extends AbstractConfig {

    private String value;

    private String key;

    private String fileExtension;

    @Override
    public String prefix() {
        return "";
    }

    /**
     * File name string.
     *
     * @return the string
     */
    public String fileName() {
        return key + "." + fileExtension;
    }
}
