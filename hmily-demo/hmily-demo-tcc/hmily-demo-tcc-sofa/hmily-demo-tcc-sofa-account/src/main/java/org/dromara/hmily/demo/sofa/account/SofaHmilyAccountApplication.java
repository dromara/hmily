package org.dromara.hmily.demo.sofa.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * The SofaHmilyAccountApplication.
 *
 * @author bbaiggey
 */
@ImportResource({ "classpath*:invoke-server-example.xml", "classpath*:invoke-client-example.xml"})
@SpringBootApplication
@MapperScan("org.dromara.hmily.demo.common.account.mapper")
public class SofaHmilyAccountApplication {
    
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
          SpringApplication springApplication = new SpringApplication(SofaHmilyAccountApplication.class);
          springApplication.setWebApplicationType(WebApplicationType.NONE);
          springApplication.run(args);
    }
}