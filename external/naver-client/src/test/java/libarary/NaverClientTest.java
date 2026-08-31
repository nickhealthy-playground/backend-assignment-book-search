package libarary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = NaverClientTest.TestConfig.class)
@ActiveProfiles("test")
class NaverClientTest {

    @EnableAutoConfiguration
    @EnableFeignClients(clients = libarary.feign.NaverClient.class)
    @ComponentScan(basePackageClasses = NaverClient.class)
    static class TestConfig {}

    @Autowired
    libarary.feign.NaverClient feignClient;

    @Test
    void callNaver() {
        String http = feignClient.search("HTTP", 1, 10);
        System.out.println("http = " + http);

        assertFalse(http.isEmpty());
    }
}