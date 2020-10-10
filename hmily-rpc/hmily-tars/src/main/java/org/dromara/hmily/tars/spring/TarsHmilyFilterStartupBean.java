package org.dromara.hmily.tars.spring;

import com.qq.tars.common.FilterKind;
import com.qq.tars.server.core.AppContextManager;
import org.dromara.hmily.tars.filter.TarsHmilyTransactionFilter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * add hmily filter to tars server.
 *
 * @author tydhot
 */
public class TarsHmilyFilterStartupBean implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        AppContextManager.getInstance().getAppContext().addFilter(FilterKind.SERVER, new TarsHmilyTransactionFilter());
    }

}
