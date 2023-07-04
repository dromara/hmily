package org.dromara.hmily.tac.sqlrevert.core.image.impl;

import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLManipulation;
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLTuple;
import org.dromara.hmily.tac.sqlrevert.core.image.RevertSQLUnit;
import org.dromara.hmily.tac.sqlrevert.core.image.SQLImageMapper;
import org.dromara.hmily.tac.sqlrevert.core.image.SQLImageMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author zhangzhi
 * @Date: 2023/7/4 15:59
 */
public class SQLImageMapperTest {

    private SQLImageMapper sqlImageMapper;

    @Test
    public void testInsertSQLImageMapper() {
        // insert sql: insert `t_user` (ID,NAME,AGE) values ('1','zhangsan',23)

        // revertSql:  DELETE FROM `t_user` WHERE ID=? AND NAME=? AND AGE=?
        // parameters: [1, zhangsan, 23]
        RevertSQLUnit targetRevertSQLUnit = new RevertSQLUnit(
                "DELETE FROM `t_user` WHERE ID=? AND NAME=? AND AGE=?",
                Arrays.asList("1", "zhangsan", 23));

        // 创建测试 insert sql 的 HmilySQLTuple 对象
        HmilySQLTuple hmilySQLTuple = new HmilySQLTuple();
        hmilySQLTuple.setTableName("t_user");
        Map<String, Object> afterImage = new HashMap<>();
        afterImage.put("ID", "1");
        afterImage.put("NAME", "zhangsan");
        afterImage.put("AGE", "23");
        hmilySQLTuple.setAfterImage(afterImage);
        hmilySQLTuple.setManipulationType(HmilySQLManipulation.INSERT);
        sqlImageMapper = SQLImageMapperFactory.newInstance(hmilySQLTuple);

        RevertSQLUnit revertSQLUnit = sqlImageMapper.cast();

        Assert.assertEquals(revertSQLUnit.getSql(), targetRevertSQLUnit.getSql());
        Assert.assertEquals(revertSQLUnit.getParameters().toString(), targetRevertSQLUnit.getParameters().toString());
    }

    @Test
    public void testDeleteSQLImageMapper() {
        // delete sql: DELETE FROM `t_user` WHERE ID='1' AND NAME='zhangsan' AND AGE=23

        // revertSql:  INSERT `t_user` (ID,NAME,AGE) VALUES (?,?,?)
        // parameters: [1, zhangsan, 23]
        RevertSQLUnit targetRevertSQLUnit = new RevertSQLUnit(
                "INSERT `t_user` (ID,NAME,AGE) VALUES (?,?,?)",
                Arrays.asList("1", "zhangsan", 23));

        //  创建测试 delete sql 的 HmilySQLTuple 对象
        HmilySQLTuple hmilySQLTuple = new HmilySQLTuple();
        hmilySQLTuple.setTableName("t_user");
        Map<String, Object> beforeImage = new HashMap<>();
        beforeImage.put("ID", "1");
        beforeImage.put("NAME", "zhangsan");
        beforeImage.put("AGE", "23");
        hmilySQLTuple.setBeforeImage(beforeImage);
        hmilySQLTuple.setManipulationType(HmilySQLManipulation.DELETE);
        sqlImageMapper = SQLImageMapperFactory.newInstance(hmilySQLTuple);

        RevertSQLUnit revertSQLUnit = sqlImageMapper.cast();

        Assert.assertEquals(revertSQLUnit.getSql(), targetRevertSQLUnit.getSql());
        Assert.assertEquals(revertSQLUnit.getParameters().toString(), targetRevertSQLUnit.getParameters().toString());
    }
}
