package libarary.feign

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles
import spock.lang.Ignore
import spock.lang.Specification


@Ignore
@SpringBootTest(classes = NaverClientIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
class NaverClientIntegrationTest extends Specification {
    @EnableAutoConfiguration
    @EnableFeignClients(clients = NaverClient.class)
    @ComponentScan(basePackageClasses = NaverClient.class)
    static class TestConfig {}

    @Autowired
    NaverClient naverClient;

    def "naver 호출"() {
        given:
        when:
        def response = naverClient.search("HTTP", 1, 10)

        then:
        print response
    }
}
