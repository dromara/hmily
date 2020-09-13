package org.dromara.hmily.demo.sofa.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * The SofaHmilyOrderApplication.
 *
 * @Author: bbaiggey
 */
@SpringBootApplication
@ImportResource({ "classpath*:invoke-client-example.xml"})
@MapperScan("org.dromara.hmily.demo.common.order.mapper")
public class SofaHmilyOrderApplication {

    /**
     * main.
     *
     * @param args args.
     */
    public static void main(final String[] args) {
        SpringApplication springApplication = new SpringApplication(SofaHmilyOrderApplication.class);
        springApplication.run(args);
    }

}
