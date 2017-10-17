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
package com.happylifeplat.tcc.core.bean.entity;

import java.io.Serializable;


/**
 * @author xiaoyu
 */
public class Participant implements Serializable {

    private static final long serialVersionUID = -2590970715288987627L;
    private String transId;

    private TccInvocation confirmTccInvocation;

    private TccInvocation cancelTccInvocation;

    public Participant() {

    }


    public Participant(String transId, TccInvocation confirmTccInvocation, TccInvocation cancelTccInvocation) {
        this.transId = transId;
        this.confirmTccInvocation = confirmTccInvocation;
        this.cancelTccInvocation = cancelTccInvocation;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public TccInvocation getConfirmTccInvocation() {
        return confirmTccInvocation;
    }

    public TccInvocation getCancelTccInvocation() {
        return cancelTccInvocation;
    }

}
