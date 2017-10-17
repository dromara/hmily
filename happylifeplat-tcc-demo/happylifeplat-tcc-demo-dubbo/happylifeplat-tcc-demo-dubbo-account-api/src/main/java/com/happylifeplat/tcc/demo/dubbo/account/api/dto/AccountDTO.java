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

package com.happylifeplat.tcc.demo.dubbo.account.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author xiaoyu
 */
public class AccountDTO implements Serializable {

    private static final long serialVersionUID = 7223470850578998427L;
    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 扣款金额
     */
    private BigDecimal amount;


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    @Override
    public String toString() {
        return "AccountDTO{" +
                "userId=" + userId +
                ", amount=" + amount +
                '}';
    }
}
