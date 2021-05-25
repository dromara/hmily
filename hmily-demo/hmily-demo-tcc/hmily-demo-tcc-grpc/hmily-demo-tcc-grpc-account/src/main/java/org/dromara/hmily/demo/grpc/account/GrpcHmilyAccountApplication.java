package org.dromara.hmily.demo.grpc.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author tydhot
 **/
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("org.dromara.hmily.demo.common.account.mapper")
public class GrpcHmilyAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrpcHmilyAccountApplication.class, args);
    }

}
