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

package com.happylifeplat.tcc.common.utils.httpclient;


import org.apache.commons.lang3.StringUtils;

/**
 * 错误码定义类
 * 所有错误码使用10进制进行定义,0-20为系统全局保留使用,
 * 其它各子系统分配99个错误码使用.<br/>
 * 错误码组成:由系统编码:+4位顺序号组成:0000+0000共8位
 * 错误码定义范围<br/>
 * 负数为非明确定义错误
 * @author xiaoyu
 * @version 1.0
 * @date 2017 /3/1 11:52
 * @since JDK 1.8
 **/
public class CommonErrorCode {


    /**
     * 操作失败全局定义定义
     */
    public static final int ERROR = -2;

    /**
     * 成功
     */
    public static final int SUCCESSFUL = 200;

    /**
     * 传入的参数错误
     */
    public static final int PARAMS_ERROR = 10000002;


    /**
     * 获取错误码描述信息
     *
     * @param code 错误码枚举的name,指向自定义的信息
     * @return 描述信息
     */
    public static final String getErrorMsg(int code) {
        //获取错误信息
        String msg = System.getProperty(String.valueOf(code));
        if (StringUtils.isBlank(msg)) {
            return "根据传入的错误码[" + code + "]没有找到相应的错误信息.";
        }
        return msg;
    }
}
