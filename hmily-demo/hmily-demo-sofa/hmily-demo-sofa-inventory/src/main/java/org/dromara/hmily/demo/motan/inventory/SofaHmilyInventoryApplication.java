package org.dromara.hmily.demo.motan.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * The SofaHmilyInventoryApplication.
 *
 * @Author: bbaiggey
 */
@SpringBootApplication
@ImportResource({ "classpath*:invoke-server-example.xml"})
@MapperScan("org.dromara.hmily.demo.common.inventory.mapper")
public class SofaHmilyInventoryApplication {

    /**
     * main.
     *
     * @param args args.
     */
    public static void main(final String[] args) {
        SpringApplication springApplication = new SpringApplication(SofaHmilyInventoryApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

}
