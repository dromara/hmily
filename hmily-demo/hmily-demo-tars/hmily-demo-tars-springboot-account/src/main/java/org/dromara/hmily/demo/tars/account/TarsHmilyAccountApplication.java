package org.dromara.hmily.demo.tars.account;

import com.qq.tars.spring.annotation.EnableTarsServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTarsServer
@MapperScan("org.dromara.hmily.demo.common.account.mapper")
public class TarsHmilyAccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(TarsHmilyAccountApplication.class, args);
    }
}
