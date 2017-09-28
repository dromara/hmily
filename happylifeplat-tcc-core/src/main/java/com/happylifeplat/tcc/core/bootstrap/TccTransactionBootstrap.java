/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.bootstrap;

import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.core.helper.SpringBeanUtils;
import com.happylifeplat.tcc.core.service.TccInitService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class TccTransactionBootstrap extends TccConfig implements ApplicationContextAware {


    private final TccInitService tccInitService;

    @Autowired
    public TccTransactionBootstrap(TccInitService tccInitService) {
        this.tccInitService = tccInitService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.getInstance().setCfgContext((ConfigurableApplicationContext) applicationContext);
        start(this);
    }


    private void start(TccConfig tccConfig) {
        tccInitService.initialization(tccConfig);
    }
}
