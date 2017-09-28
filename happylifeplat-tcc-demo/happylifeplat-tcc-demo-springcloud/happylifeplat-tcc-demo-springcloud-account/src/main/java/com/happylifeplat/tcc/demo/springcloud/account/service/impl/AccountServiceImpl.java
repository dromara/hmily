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

package com.happylifeplat.tcc.demo.springcloud.account.service.impl;

import com.happylifeplat.tcc.annotation.Tcc;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;

import com.happylifeplat.tcc.demo.springcloud.account.dto.AccountDTO;
import com.happylifeplat.tcc.demo.springcloud.account.entity.AccountDO;
import com.happylifeplat.tcc.demo.springcloud.account.mapper.AccountMapper;
import com.happylifeplat.tcc.demo.springcloud.account.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);


    private final AccountMapper accountMapper;

    @Autowired
    public AccountServiceImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 扣款支付
     *
     * @param accountDTO 参数dto
     * @return true
     */
    @Override
    @Tcc(confirmMethod = "confirm", cancelMethod = "cancel")
    public boolean payment(AccountDTO accountDTO) {
        LOGGER.debug("============springcloud执行try付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().subtract(accountDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().add(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        final int update = accountMapper.update(accountDO);
        if (update != 1) {
            throw new TccRuntimeException("资金不足！");
        }
        return Boolean.TRUE;
    }

    public boolean confirm(AccountDTO accountDTO) {

        LOGGER.debug("============springcloud执行确认付款接口===============");

        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.update(accountDO);
        return Boolean.TRUE;
    }


    public boolean cancel(AccountDTO accountDTO) {

        LOGGER.debug("============springcloud执行取消付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        accountDO.setBalance(accountDO.getBalance().add(accountDTO.getAmount()));
        accountDO.setFreezeAmount(accountDO.getFreezeAmount().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        accountMapper.update(accountDO);
        return Boolean.TRUE;
    }
}
