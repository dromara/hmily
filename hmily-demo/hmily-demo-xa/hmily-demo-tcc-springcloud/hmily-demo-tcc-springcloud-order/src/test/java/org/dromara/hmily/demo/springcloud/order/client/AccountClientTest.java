package org.dromara.hmily.demo.springcloud.order.client;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountClientTest {
    @Autowired
    private AccountClient client;

    @Test
    public void payment() {
        System.out.println (1);
        System.out.println (client);
    }

}

