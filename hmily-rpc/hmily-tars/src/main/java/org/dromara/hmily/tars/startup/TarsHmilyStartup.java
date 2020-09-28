package org.dromara.hmily.tars.startup;

import com.qq.tars.common.FilterKind;
import com.qq.tars.server.core.AppContextManager;
import org.dromara.hmily.tars.filter.TarsHmilyTransactionFilter;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * add hmily filter to tars server.
 *
 * @author tydhot
 */
public class TarsHmilyStartup implements CommandLineRunner, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(final String... args) {
        AppContextManager.getInstance().getAppContext().addFilter(FilterKind.SERVER, new TarsHmilyTransactionFilter());
    }

}
