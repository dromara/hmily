package org.dromara.hmily.demo.grpc.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author tydhot
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("org.dromara.hmily.demo.common.inventory.mapper")
public class GrpcHmilyInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcHmilyInventoryApplication.class, args);
    }

}
