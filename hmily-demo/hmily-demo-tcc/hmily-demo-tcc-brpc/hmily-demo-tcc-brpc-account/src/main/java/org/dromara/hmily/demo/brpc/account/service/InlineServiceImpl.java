package org.dromara.hmily.demo.brpc.account.service;

import com.baidu.brpc.spring.annotation.RpcExporter;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.demo.common.account.api.InlineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InlineServiceImpl.
 *
 * @author liu·yu
 */
@RpcExporter
public class InlineServiceImpl implements InlineService {


    private static final Logger LOGGER = LoggerFactory.getLogger(InlineServiceImpl.class);

    @Override
    @HmilyTCC(confirmMethod = "inLineConfirm", cancelMethod = "inLineCancel")
    public boolean testInline() {
        LOGGER.info("===========执行inline try 方法==============");
        return true;
    }

    public void inLineConfirm() {
        LOGGER.info("===========执行inlineConfirm 方法==============");
    }


    public void inLineCancel() {
        LOGGER.info("===========执行inLineCancel 方法==============");
    }

}
