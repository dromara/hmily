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
import com.happylifeplat.tcc.core.coordinator.CoordinatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xiaoyu
 */
@Component
public class CoordinatorCommand implements Command {


    private final CoordinatorService coordinatorService;

    @Autowired
    public CoordinatorCommand(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }


    /**
     * 执行协调命令接口
     *
     * @param coordinatorAction 协调数据
     */
    @Override
    public void execute(CoordinatorAction coordinatorAction) {
        coordinatorService.submit(coordinatorAction);
    }


}
