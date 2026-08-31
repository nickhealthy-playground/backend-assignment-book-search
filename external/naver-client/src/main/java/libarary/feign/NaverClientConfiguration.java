package libarary.feign;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

public class NaverClientConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor(
            @Value("${external.naver.headers.client-id}") String clientId,
            @Value("${external.naver.headers.client-secret}") String clientSecret) {

        return requestTemplate -> requestTemplate
                .header("X-Naver-client-Id", clientId)
                .header("X-Naver-client-Secret", clientSecret);
    }

    @Bean
    public NaverErrorDecoder naverErrorDecoder(ObjectMapper objectMapper) {
        return new NaverErrorDecoder(objectMapper);
    }
}
