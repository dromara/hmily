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

import com.google.common.collect.Lists;
import com.happylifeplat.tcc.common.utils.IdWorkerUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * @author xiaoyu
 */
public class TccTransaction implements Serializable {


    private static final long serialVersionUID = -6792063780987394917L;

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

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    /**
     * 参与协调的方法集合
     */
    private List<Participant> participants;


    public TccTransaction() {
        this.transId = IdWorkerUtils.getInstance().createUUID();
        this.createTime = new Date();
        this.lastTime = new Date();
        participants = Lists.newCopyOnWriteArrayList();

    }

    public TccTransaction(String transId) {
        this.transId = transId;
        this.createTime = new Date();
        this.lastTime = new Date();
        participants = Lists.newCopyOnWriteArrayList();
    }

    public void registerParticipant(Participant participant) {
        Assert.notNull(participants, "participants is null");
        participants.add(participant);
    }


    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }


    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public Integer getPattern() {
        return pattern;
    }

    public void setPattern(Integer pattern) {
        this.pattern = pattern;
    }
}
