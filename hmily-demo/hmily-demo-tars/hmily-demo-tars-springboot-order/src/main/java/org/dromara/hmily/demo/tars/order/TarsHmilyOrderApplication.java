package org.dromara.hmily.demo.tars.order;

import com.qq.tars.spring.annotation.EnableTarsServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTarsServer
@MapperScan("org.dromara.hmily.demo.common.order.mapper")
public class TarsHmilyOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(TarsHmilyOrderApplication.class, args);
    }
}
