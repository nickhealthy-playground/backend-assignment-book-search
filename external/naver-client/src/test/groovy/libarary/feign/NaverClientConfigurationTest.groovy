package libarary.feign

import feign.RequestInterceptor
import feign.RequestTemplate
import spock.lang.Specification

class NaverClientConfigurationTest extends Specification {
    NaverClientConfiguration configuration

    void setup() {
        configuration = new NaverClientConfiguration()
    }

    def "requestInterceptor의 header에 key 값들이 존재한다."() {
        given:
        def template = new RequestTemplate()
        def clientId = "id"
        def clientSecret = "secret"

        and: "interceptor를 타기 전에 header가 존재하지 않는다."
        template.headers()["X-Naver-client-Id"] == null
        template.headers()["X-Naver-client-Secret"] == null

        when: "interceptor를 탄다."
        def interceptor = configuration.requestInterceptor(clientId, clientSecret)
        interceptor.apply(template)

        then: "interceptor를 탄 이후에는 header가 추가된다."
        template.headers()["X-Naver-client-Id"].contains(clientId)
        template.headers()["X-Naver-client-Secret"].contains(clientSecret)

    }
}
