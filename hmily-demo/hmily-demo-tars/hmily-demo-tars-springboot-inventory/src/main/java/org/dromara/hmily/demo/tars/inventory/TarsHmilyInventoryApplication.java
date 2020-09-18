package org.dromara.hmily.demo.tars.inventory;

import com.qq.tars.spring.annotation.EnableTarsServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableTarsServer
@MapperScan("org.dromara.hmily.demo.common.inventory.mapper")
public class TarsHmilyInventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(TarsHmilyInventoryApplication.class, args);
    }
}
