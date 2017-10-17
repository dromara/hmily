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
package com.happylifeplat.tcc.core.bean;

import com.happylifeplat.tcc.common.enums.TccActionEnum;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;


/**
 * @author xiaoyu
 */
public class MongoBean implements Serializable {

    private static final long serialVersionUID = 7920817865031921102L;


    private ObjectId id;


    /**
     * 事务主键id
     */
    private String transId;


    /**
     * 重试次数，
     */
    private int retriedCount = 0;

    /**
     * 创建时间
     */
    private Date createTime = new Date();


    /**
     * 创建时间
     */
    private Date lastTime = new Date();

    /**
     * 版本控制 防止并发问题
     */
    private int version = 1;


    /**
     * 序列化后的二进制信息
     */
    private byte[] contents;

    /**
     * {@linkplain TccActionEnum}
     */
    private int status;


    private Integer role;

    private Integer pattern;


    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public int getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getPattern() {
        return pattern;
    }

    public void setPattern(Integer pattern) {
        this.pattern = pattern;
    }
}
