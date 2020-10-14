package org.dromara.hmily.demo.grpc.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author tydhot
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("org.dromara.hmily.demo.common.order.mapper")
public class GrpcHmilyOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrpcHmilyOrderApplication.class, args);
    }
}
