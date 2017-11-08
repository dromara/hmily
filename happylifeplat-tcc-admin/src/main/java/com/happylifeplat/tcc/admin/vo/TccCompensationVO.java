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

package com.happylifeplat.tcc.admin.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/19 16:37
 * @since JDK 1.8
 */
@Data
public class TccCompensationVO implements Serializable {


    private static final long serialVersionUID = 564418979137349581L;


    private String transId;


    /**
     * 重试次数，
     */
    private Integer retriedCount;

    /**
     * 创建时间
     */
    private String createTime;


    /**
     * 创建时间
     */
    private String lastTime;

    /**
     * 版本控制 防止并发问题
     */
    private Integer version;


    /**
     * 执行类名称
     */
    private String targetClass;
    /**
     * 执行方法
     */
    private String targetMethod;

    /**
     * 确认方法
     */
    private String confirmMethod;

    /**
     * 取消方法
     */
    private String cancelMethod;


}
