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

package com.happylifeplat.tcc.common.bean.adapter;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/30 10:39
 * @since JDK 1.8
 */
@Data
@NoArgsConstructor
public class CoordinatorRepositoryAdapter {


    /**
     * 事务id
     */
    private String transId;

    /**
     * 事务状态 {@linkplain com.happylifeplat.tcc.common.enums.TccActionEnum}
     */
    private int status;

    /**
     * 事务类型 {@linkplain com.happylifeplat.tcc.common.enums.TccRoleEnum}
     */
    private int role;

    /**
     * 重试次数
     */
    private volatile int retriedCount = 0;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date lastTime;

    /**
     * 版本号 乐观锁控制
     */
    private Integer version = 1;

    /**
     * 模式
     */
    private Integer pattern;

    /**
     * 序列化后的二进制信息
     */
    private byte[] contents;



    /**
     * 调用接口名称
     */
    private String targetClass;


    /**
     * 调用方法名称
     */
    private String targetMethod;

    /**
     * confirm方法
     */
    private String confirmMethod;


    /**
     * cancel方法
     */
    private String cancelMethod;


}
