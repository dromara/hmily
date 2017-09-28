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
package com.happylifeplat.tcc.core.concurrent.threadlocal;

public class CoordinatorLocal {

    private static final CoordinatorLocal COMPENSATION_LOCAL = new CoordinatorLocal();

    private CoordinatorLocal() {

    }

    public static CoordinatorLocal getInstance() {
        return COMPENSATION_LOCAL;
    }


    private static final ThreadLocal<String> currentLocal = new ThreadLocal<>();


    public void set(String coordinatorId) {
        currentLocal.set(coordinatorId);
    }

    public String get() {
        return currentLocal.get();
    }

    public void remove() {
        currentLocal.remove();
    }

}
