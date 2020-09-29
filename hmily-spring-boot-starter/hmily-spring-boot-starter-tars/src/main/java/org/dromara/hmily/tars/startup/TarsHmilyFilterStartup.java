package org.dromara.hmily.tars.startup;

import com.qq.tars.common.FilterKind;
import com.qq.tars.server.core.AppContextManager;
import org.dromara.hmily.tars.filter.TarsHmilyTransactionFilter;
import org.springframework.boot.CommandLineRunner;

/**
 * add hmily filter to tars server.
 *
 * @author tydhot
 */
public class TarsHmilyFilterStartup implements CommandLineRunner {

    @Override
    public void run(final String... args) {
        AppContextManager.getInstance().getAppContext().addFilter(FilterKind.SERVER, new TarsHmilyTransactionFilter());
    }

}
