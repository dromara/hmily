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


import java.io.Serializable;

/**
 * @author  xiaoyu
 * @version 1.0
 * @date 2017 /3/1 11:52
 * @since JDK 1.8
 **/
public class AjaxResponse implements Serializable {

    private static final long serialVersionUID = -2792556188993845048L;

    protected int code;
    protected String message;
    private Object data;

    /**
     * @param code <font color="red">非-1的数</font>
     */
    protected AjaxResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static AjaxResponse success() {
        return success("");
    }

    public static AjaxResponse success(String msg) {
        return success(msg, null);
    }

    public static AjaxResponse success(Object data) {
        return success(null, data);
    }

    public static AjaxResponse success(String msg, Object data) {
        return get(CommonErrorCode.SUCCESSFUL, msg, data);
    }

    public static AjaxResponse error(String msg) {
        return error(CommonErrorCode.ERROR, msg);
    }

    /**
     * 响应错误
     *
     * @param code <font color="red">非-1的数</font>
     * @param msg
     * @return
     */
    public static AjaxResponse error(int code, String msg) {
        return get(code, msg, null);
    }

    public static AjaxResponse get(int code, String msg, Object data) {
        return new AjaxResponse(code, msg, data);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AjaxResponse [code=" + code + ", message=" + message + ", data=" + data
                + "]";
    }

}
