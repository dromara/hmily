package org.dromara.hmily.demo.motan.account.service;

import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.demo.motan.account.api.service.InlineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * InlineServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
@Service("inlineService")
public class InlineServiceImpl implements InlineService {


    private static final Logger LOGGER = LoggerFactory.getLogger(InlineServiceImpl.class);

    @Override
    @HmilyTCC(confirmMethod = "inLineConfirm", cancelMethod = "inLineCancel")
    public void testInline() {
        LOGGER.info("===========执行inline try 方法==============");
    }

    public void inLineConfirm() {
        LOGGER.info("===========执行inlineConfirm 方法==============");
    }


    public void inLineCancel() {
        LOGGER.info("===========执行inLineCancel 方法==============");
    }

}
