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

package com.happylifeplat.tcc.core.coordinator.command;

import com.happylifeplat.tcc.common.enums.CoordinatorActionEnum;
import com.happylifeplat.tcc.core.bean.entity.TccTransaction;

import java.io.Serializable;

/**
 * @author xiaoyu
 */
public class CoordinatorAction implements Serializable {


    private static final long serialVersionUID = -5714963272234819587L;

    private CoordinatorActionEnum action;


    private TccTransaction tccTransaction;

    public CoordinatorAction(CoordinatorActionEnum action, TccTransaction tccTransaction) {
        this.action = action;
        this.tccTransaction = tccTransaction;
    }


    public CoordinatorActionEnum getAction() {
        return action;
    }

    public void setAction(CoordinatorActionEnum action) {
        this.action = action;
    }

    public TccTransaction getTccTransaction() {
        return tccTransaction;
    }

    public void setTccTransaction(TccTransaction tccTransaction) {
        this.tccTransaction = tccTransaction;
    }
}
