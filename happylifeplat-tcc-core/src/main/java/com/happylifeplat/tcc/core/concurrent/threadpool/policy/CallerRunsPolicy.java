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
package com.happylifeplat.tcc.core.concurrent.threadpool.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xiaoyu
 */
public class CallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(CallerRunsPolicy.class);

    private String threadName;

    public CallerRunsPolicy() {
        this(null);
    }

    public CallerRunsPolicy(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        if (threadName != null) {
            LOG.error("tccTransaction Thread pool [{}] is exhausted, executor={}", threadName, executor.toString());
        }

        super.rejectedExecution(runnable, executor);
    }
}

